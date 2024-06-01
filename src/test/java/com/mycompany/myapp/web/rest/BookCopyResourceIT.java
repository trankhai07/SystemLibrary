package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.BookCopy;
import com.mycompany.myapp.repository.BookCopyRepository;
import com.mycompany.myapp.repository.search.BookCopySearchRepository;
import com.mycompany.myapp.service.BookCopyService;
import java.util.ArrayList;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Integration tests for the {@link BookCopyResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class BookCopyResourceIT {

    private static final Integer DEFAULT_YEAR_PUBLISHED = 1;
    private static final Integer UPDATED_YEAR_PUBLISHED = 2;

    private static final Integer DEFAULT_AMOUNT = 1;
    private static final Integer UPDATED_AMOUNT = 2;

    private static final String DEFAULT_IMAGE = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/book-copies";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/book-copies";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BookCopyRepository bookCopyRepositoryMock;

    @Mock
    private BookCopyService bookCopyServiceMock;

    @Autowired
    private BookCopySearchRepository bookCopySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBookCopyMockMvc;

    private BookCopy bookCopy;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BookCopy createEntity(EntityManager em) {
        BookCopy bookCopy = new BookCopy()
            .yearPublished(DEFAULT_YEAR_PUBLISHED)
            .amount(DEFAULT_AMOUNT)
            .image(DEFAULT_IMAGE)
            .description(DEFAULT_DESCRIPTION);
        return bookCopy;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BookCopy createUpdatedEntity(EntityManager em) {
        BookCopy bookCopy = new BookCopy()
            .yearPublished(UPDATED_YEAR_PUBLISHED)
            .amount(UPDATED_AMOUNT)
            .image(UPDATED_IMAGE)
            .description(UPDATED_DESCRIPTION);
        return bookCopy;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        bookCopySearchRepository.deleteAll();
        assertThat(bookCopySearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        bookCopy = createEntity(em);
    }

    @Test
    @Transactional
    void createBookCopy() throws Exception {
        int databaseSizeBeforeCreate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        // Create the BookCopy
        restBookCopyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bookCopy)))
            .andExpect(status().isCreated());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        BookCopy testBookCopy = bookCopyList.get(bookCopyList.size() - 1);
        assertThat(testBookCopy.getYearPublished()).isEqualTo(DEFAULT_YEAR_PUBLISHED);
        assertThat(testBookCopy.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testBookCopy.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testBookCopy.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void createBookCopyWithExistingId() throws Exception {
        // Create the BookCopy with an existing ID
        bookCopy.setId(1L);

        int databaseSizeBeforeCreate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookCopyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bookCopy)))
            .andExpect(status().isBadRequest());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllBookCopies() throws Exception {
        // Initialize the database
        bookCopyRepository.saveAndFlush(bookCopy);

        // Get all the bookCopyList
        restBookCopyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bookCopy.getId().intValue())))
            .andExpect(jsonPath("$.[*].yearPublished").value(hasItem(DEFAULT_YEAR_PUBLISHED)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(DEFAULT_IMAGE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBookCopiesWithEagerRelationshipsIsEnabled() throws Exception {
        when(bookCopyServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBookCopyMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(bookCopyServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBookCopiesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(bookCopyServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBookCopyMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(bookCopyRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getBookCopy() throws Exception {
        // Initialize the database
        bookCopyRepository.saveAndFlush(bookCopy);

        // Get the bookCopy
        restBookCopyMockMvc
            .perform(get(ENTITY_API_URL_ID, bookCopy.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bookCopy.getId().intValue()))
            .andExpect(jsonPath("$.yearPublished").value(DEFAULT_YEAR_PUBLISHED))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
            .andExpect(jsonPath("$.image").value(DEFAULT_IMAGE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingBookCopy() throws Exception {
        // Get the bookCopy
        restBookCopyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBookCopy() throws Exception {
        // Initialize the database
        bookCopyRepository.saveAndFlush(bookCopy);

        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        bookCopySearchRepository.save(bookCopy);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());

        // Update the bookCopy
        BookCopy updatedBookCopy = bookCopyRepository.findById(bookCopy.getId()).get();
        // Disconnect from session so that the updates on updatedBookCopy are not directly saved in db
        em.detach(updatedBookCopy);
        updatedBookCopy.yearPublished(UPDATED_YEAR_PUBLISHED).amount(UPDATED_AMOUNT).image(UPDATED_IMAGE).description(UPDATED_DESCRIPTION);

        restBookCopyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedBookCopy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedBookCopy))
            )
            .andExpect(status().isOk());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        BookCopy testBookCopy = bookCopyList.get(bookCopyList.size() - 1);
        assertThat(testBookCopy.getYearPublished()).isEqualTo(UPDATED_YEAR_PUBLISHED);
        assertThat(testBookCopy.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBookCopy.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testBookCopy.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<BookCopy> bookCopySearchList = IterableUtils.toList(bookCopySearchRepository.findAll());
                BookCopy testBookCopySearch = bookCopySearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testBookCopySearch.getYearPublished()).isEqualTo(UPDATED_YEAR_PUBLISHED);
                assertThat(testBookCopySearch.getAmount()).isEqualTo(UPDATED_AMOUNT);
                assertThat(testBookCopySearch.getImage()).isEqualTo(UPDATED_IMAGE);
                assertThat(testBookCopySearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
            });
    }

    @Test
    @Transactional
    void putNonExistingBookCopy() throws Exception {
        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        bookCopy.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookCopyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, bookCopy.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(bookCopy))
            )
            .andExpect(status().isBadRequest());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchBookCopy() throws Exception {
        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        bookCopy.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookCopyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(bookCopy))
            )
            .andExpect(status().isBadRequest());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBookCopy() throws Exception {
        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        bookCopy.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookCopyMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bookCopy)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateBookCopyWithPatch() throws Exception {
        // Initialize the database
        bookCopyRepository.saveAndFlush(bookCopy);

        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();

        // Update the bookCopy using partial update
        BookCopy partialUpdatedBookCopy = new BookCopy();
        partialUpdatedBookCopy.setId(bookCopy.getId());

        partialUpdatedBookCopy.amount(UPDATED_AMOUNT).image(UPDATED_IMAGE);

        restBookCopyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBookCopy.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBookCopy))
            )
            .andExpect(status().isOk());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        BookCopy testBookCopy = bookCopyList.get(bookCopyList.size() - 1);
        assertThat(testBookCopy.getYearPublished()).isEqualTo(DEFAULT_YEAR_PUBLISHED);
        assertThat(testBookCopy.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBookCopy.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testBookCopy.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateBookCopyWithPatch() throws Exception {
        // Initialize the database
        bookCopyRepository.saveAndFlush(bookCopy);

        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();

        // Update the bookCopy using partial update
        BookCopy partialUpdatedBookCopy = new BookCopy();
        partialUpdatedBookCopy.setId(bookCopy.getId());

        partialUpdatedBookCopy
            .yearPublished(UPDATED_YEAR_PUBLISHED)
            .amount(UPDATED_AMOUNT)
            .image(UPDATED_IMAGE)
            .description(UPDATED_DESCRIPTION);

        restBookCopyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBookCopy.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBookCopy))
            )
            .andExpect(status().isOk());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        BookCopy testBookCopy = bookCopyList.get(bookCopyList.size() - 1);
        assertThat(testBookCopy.getYearPublished()).isEqualTo(UPDATED_YEAR_PUBLISHED);
        assertThat(testBookCopy.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBookCopy.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testBookCopy.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingBookCopy() throws Exception {
        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        bookCopy.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookCopyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, bookCopy.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(bookCopy))
            )
            .andExpect(status().isBadRequest());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBookCopy() throws Exception {
        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        bookCopy.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookCopyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(bookCopy))
            )
            .andExpect(status().isBadRequest());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBookCopy() throws Exception {
        int databaseSizeBeforeUpdate = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        bookCopy.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookCopyMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(bookCopy)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BookCopy in the database
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteBookCopy() throws Exception {
        // Initialize the database
        bookCopyRepository.saveAndFlush(bookCopy);
        bookCopyRepository.save(bookCopy);
        bookCopySearchRepository.save(bookCopy);

        int databaseSizeBeforeDelete = bookCopyRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the bookCopy
        restBookCopyMockMvc
            .perform(delete(ENTITY_API_URL_ID, bookCopy.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<BookCopy> bookCopyList = bookCopyRepository.findAll();
        assertThat(bookCopyList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(bookCopySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchBookCopy() throws Exception {
        // Initialize the database
        bookCopy = bookCopyRepository.saveAndFlush(bookCopy);
        bookCopySearchRepository.save(bookCopy);

        // Search the bookCopy
        restBookCopyMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + bookCopy.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bookCopy.getId().intValue())))
            .andExpect(jsonPath("$.[*].yearPublished").value(hasItem(DEFAULT_YEAR_PUBLISHED)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(DEFAULT_IMAGE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
}
