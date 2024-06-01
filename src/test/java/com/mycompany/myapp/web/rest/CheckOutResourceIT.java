package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.CheckOut;
import com.mycompany.myapp.domain.enumeration.Status;
import com.mycompany.myapp.repository.CheckOutRepository;
import com.mycompany.myapp.repository.search.CheckOutSearchRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CheckOutResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CheckOutResourceIT {

    private static final Instant DEFAULT_START_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Status DEFAULT_STATUS = Status.Confirmed;
    private static final Status UPDATED_STATUS = Status.Canceled;

    private static final Boolean DEFAULT_IS_RETURNED = false;
    private static final Boolean UPDATED_IS_RETURNED = true;

    private static final String ENTITY_API_URL = "/api/check-outs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/check-outs";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CheckOutRepository checkOutRepository;

    @Autowired
    private CheckOutSearchRepository checkOutSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCheckOutMockMvc;

    private CheckOut checkOut;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CheckOut createEntity(EntityManager em) {
        CheckOut checkOut = new CheckOut()
            .startTime(DEFAULT_START_TIME)
            .endTime(DEFAULT_END_TIME)
            .status(DEFAULT_STATUS)
            .isReturned(DEFAULT_IS_RETURNED);
        return checkOut;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CheckOut createUpdatedEntity(EntityManager em) {
        CheckOut checkOut = new CheckOut()
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .status(UPDATED_STATUS)
            .isReturned(UPDATED_IS_RETURNED);
        return checkOut;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        checkOutSearchRepository.deleteAll();
        assertThat(checkOutSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        checkOut = createEntity(em);
    }

    @Test
    @Transactional
    void createCheckOut() throws Exception {
        int databaseSizeBeforeCreate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        // Create the CheckOut
        restCheckOutMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(checkOut)))
            .andExpect(status().isCreated());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        CheckOut testCheckOut = checkOutList.get(checkOutList.size() - 1);
        assertThat(testCheckOut.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(testCheckOut.getEndTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(testCheckOut.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testCheckOut.getIsReturned()).isEqualTo(DEFAULT_IS_RETURNED);
    }

    @Test
    @Transactional
    void createCheckOutWithExistingId() throws Exception {
        // Create the CheckOut with an existing ID
        checkOut.setId(1L);

        int databaseSizeBeforeCreate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restCheckOutMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(checkOut)))
            .andExpect(status().isBadRequest());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllCheckOuts() throws Exception {
        // Initialize the database
        checkOutRepository.saveAndFlush(checkOut);

        // Get all the checkOutList
        restCheckOutMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(checkOut.getId().intValue())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].isReturned").value(hasItem(DEFAULT_IS_RETURNED.booleanValue())));
    }

    @Test
    @Transactional
    void getCheckOut() throws Exception {
        // Initialize the database
        checkOutRepository.saveAndFlush(checkOut);

        // Get the checkOut
        restCheckOutMockMvc
            .perform(get(ENTITY_API_URL_ID, checkOut.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(checkOut.getId().intValue()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.toString()))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.isReturned").value(DEFAULT_IS_RETURNED.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingCheckOut() throws Exception {
        // Get the checkOut
        restCheckOutMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCheckOut() throws Exception {
        // Initialize the database
        checkOutRepository.saveAndFlush(checkOut);

        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        checkOutSearchRepository.save(checkOut);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());

        // Update the checkOut
        CheckOut updatedCheckOut = checkOutRepository.findById(checkOut.getId()).get();
        // Disconnect from session so that the updates on updatedCheckOut are not directly saved in db
        em.detach(updatedCheckOut);
        updatedCheckOut.startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME).status(UPDATED_STATUS).isReturned(UPDATED_IS_RETURNED);

        restCheckOutMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCheckOut.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedCheckOut))
            )
            .andExpect(status().isOk());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        CheckOut testCheckOut = checkOutList.get(checkOutList.size() - 1);
        assertThat(testCheckOut.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testCheckOut.getEndTime()).isEqualTo(UPDATED_END_TIME);
        assertThat(testCheckOut.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testCheckOut.getIsReturned()).isEqualTo(UPDATED_IS_RETURNED);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<CheckOut> checkOutSearchList = IterableUtils.toList(checkOutSearchRepository.findAll());
                CheckOut testCheckOutSearch = checkOutSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testCheckOutSearch.getStartTime()).isEqualTo(UPDATED_START_TIME);
                assertThat(testCheckOutSearch.getEndTime()).isEqualTo(UPDATED_END_TIME);
                assertThat(testCheckOutSearch.getStatus()).isEqualTo(UPDATED_STATUS);
                assertThat(testCheckOutSearch.getIsReturned()).isEqualTo(UPDATED_IS_RETURNED);
            });
    }

    @Test
    @Transactional
    void putNonExistingCheckOut() throws Exception {
        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        checkOut.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCheckOutMockMvc
            .perform(
                put(ENTITY_API_URL_ID, checkOut.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(checkOut))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchCheckOut() throws Exception {
        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        checkOut.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckOutMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(checkOut))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCheckOut() throws Exception {
        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        checkOut.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckOutMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(checkOut)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateCheckOutWithPatch() throws Exception {
        // Initialize the database
        checkOutRepository.saveAndFlush(checkOut);

        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();

        // Update the checkOut using partial update
        CheckOut partialUpdatedCheckOut = new CheckOut();
        partialUpdatedCheckOut.setId(checkOut.getId());

        partialUpdatedCheckOut.startTime(UPDATED_START_TIME);

        restCheckOutMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCheckOut.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCheckOut))
            )
            .andExpect(status().isOk());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        CheckOut testCheckOut = checkOutList.get(checkOutList.size() - 1);
        assertThat(testCheckOut.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testCheckOut.getEndTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(testCheckOut.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testCheckOut.getIsReturned()).isEqualTo(DEFAULT_IS_RETURNED);
    }

    @Test
    @Transactional
    void fullUpdateCheckOutWithPatch() throws Exception {
        // Initialize the database
        checkOutRepository.saveAndFlush(checkOut);

        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();

        // Update the checkOut using partial update
        CheckOut partialUpdatedCheckOut = new CheckOut();
        partialUpdatedCheckOut.setId(checkOut.getId());

        partialUpdatedCheckOut
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .status(UPDATED_STATUS)
            .isReturned(UPDATED_IS_RETURNED);

        restCheckOutMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCheckOut.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCheckOut))
            )
            .andExpect(status().isOk());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        CheckOut testCheckOut = checkOutList.get(checkOutList.size() - 1);
        assertThat(testCheckOut.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testCheckOut.getEndTime()).isEqualTo(UPDATED_END_TIME);
        assertThat(testCheckOut.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testCheckOut.getIsReturned()).isEqualTo(UPDATED_IS_RETURNED);
    }

    @Test
    @Transactional
    void patchNonExistingCheckOut() throws Exception {
        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        checkOut.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCheckOutMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, checkOut.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(checkOut))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCheckOut() throws Exception {
        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        checkOut.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckOutMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(checkOut))
            )
            .andExpect(status().isBadRequest());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCheckOut() throws Exception {
        int databaseSizeBeforeUpdate = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        checkOut.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCheckOutMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(checkOut)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CheckOut in the database
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteCheckOut() throws Exception {
        // Initialize the database
        checkOutRepository.saveAndFlush(checkOut);
        checkOutRepository.save(checkOut);
        checkOutSearchRepository.save(checkOut);

        int databaseSizeBeforeDelete = checkOutRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the checkOut
        restCheckOutMockMvc
            .perform(delete(ENTITY_API_URL_ID, checkOut.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CheckOut> checkOutList = checkOutRepository.findAll();
        assertThat(checkOutList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(checkOutSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchCheckOut() throws Exception {
        // Initialize the database
        checkOut = checkOutRepository.saveAndFlush(checkOut);
        checkOutSearchRepository.save(checkOut);

        // Search the checkOut
        restCheckOutMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + checkOut.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(checkOut.getId().intValue())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].isReturned").value(hasItem(DEFAULT_IS_RETURNED.booleanValue())));
    }
}
