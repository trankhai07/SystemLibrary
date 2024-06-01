package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.PatronAccount;
import com.mycompany.myapp.repository.PatronAccountRepository;
import com.mycompany.myapp.repository.search.PatronAccountSearchRepository;
import com.mycompany.myapp.service.PatronAccountService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
 * Integration tests for the {@link PatronAccountResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PatronAccountResourceIT {

    private static final String ENTITY_API_URL = "/api/patron-accounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{cardNumber}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/patron-accounts";

    @Autowired
    private PatronAccountRepository patronAccountRepository;

    @Mock
    private PatronAccountRepository patronAccountRepositoryMock;

    @Mock
    private PatronAccountService patronAccountServiceMock;

    @Autowired
    private PatronAccountSearchRepository patronAccountSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPatronAccountMockMvc;

    private PatronAccount patronAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PatronAccount createEntity(EntityManager em) {
        PatronAccount patronAccount = new PatronAccount();
        return patronAccount;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PatronAccount createUpdatedEntity(EntityManager em) {
        PatronAccount patronAccount = new PatronAccount();
        return patronAccount;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        patronAccountSearchRepository.deleteAll();
        assertThat(patronAccountSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        patronAccount = createEntity(em);
    }

    @Test
    @Transactional
    void createPatronAccount() throws Exception {
        int databaseSizeBeforeCreate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        // Create the PatronAccount
        restPatronAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(patronAccount)))
            .andExpect(status().isCreated());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        PatronAccount testPatronAccount = patronAccountList.get(patronAccountList.size() - 1);
    }

    @Test
    @Transactional
    void createPatronAccountWithExistingId() throws Exception {
        // Create the PatronAccount with an existing ID
        patronAccount.setCardNumber("existing_id");

        int databaseSizeBeforeCreate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPatronAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(patronAccount)))
            .andExpect(status().isBadRequest());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPatronAccounts() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccountRepository.saveAndFlush(patronAccount);

        // Get all the patronAccountList
        restPatronAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=cardNumber,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].cardNumber").value(hasItem(patronAccount.getCardNumber())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPatronAccountsWithEagerRelationshipsIsEnabled() throws Exception {
        when(patronAccountServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPatronAccountMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(patronAccountServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPatronAccountsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(patronAccountServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPatronAccountMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(patronAccountRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPatronAccount() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccountRepository.saveAndFlush(patronAccount);

        // Get the patronAccount
        restPatronAccountMockMvc
            .perform(get(ENTITY_API_URL_ID, patronAccount.getCardNumber()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.cardNumber").value(patronAccount.getCardNumber()));
    }

    @Test
    @Transactional
    void getNonExistingPatronAccount() throws Exception {
        // Get the patronAccount
        restPatronAccountMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPatronAccount() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccountRepository.saveAndFlush(patronAccount);

        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        patronAccountSearchRepository.save(patronAccount);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());

        // Update the patronAccount
        PatronAccount updatedPatronAccount = patronAccountRepository.findById(patronAccount.getCardNumber()).get();
        // Disconnect from session so that the updates on updatedPatronAccount are not directly saved in db
        em.detach(updatedPatronAccount);

        restPatronAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPatronAccount.getCardNumber())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPatronAccount))
            )
            .andExpect(status().isOk());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        PatronAccount testPatronAccount = patronAccountList.get(patronAccountList.size() - 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<PatronAccount> patronAccountSearchList = IterableUtils.toList(patronAccountSearchRepository.findAll());
                PatronAccount testPatronAccountSearch = patronAccountSearchList.get(searchDatabaseSizeAfter - 1);
            });
    }

    @Test
    @Transactional
    void putNonExistingPatronAccount() throws Exception {
        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        patronAccount.setCardNumber(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPatronAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, patronAccount.getCardNumber())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(patronAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPatronAccount() throws Exception {
        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        patronAccount.setCardNumber(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatronAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(patronAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPatronAccount() throws Exception {
        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        patronAccount.setCardNumber(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatronAccountMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(patronAccount)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePatronAccountWithPatch() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccountRepository.saveAndFlush(patronAccount);

        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();

        // Update the patronAccount using partial update
        PatronAccount partialUpdatedPatronAccount = new PatronAccount();
        partialUpdatedPatronAccount.setCardNumber(patronAccount.getCardNumber());

        restPatronAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPatronAccount.getCardNumber())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPatronAccount))
            )
            .andExpect(status().isOk());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        PatronAccount testPatronAccount = patronAccountList.get(patronAccountList.size() - 1);
    }

    @Test
    @Transactional
    void fullUpdatePatronAccountWithPatch() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccountRepository.saveAndFlush(patronAccount);

        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();

        // Update the patronAccount using partial update
        PatronAccount partialUpdatedPatronAccount = new PatronAccount();
        partialUpdatedPatronAccount.setCardNumber(patronAccount.getCardNumber());

        restPatronAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPatronAccount.getCardNumber())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPatronAccount))
            )
            .andExpect(status().isOk());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        PatronAccount testPatronAccount = patronAccountList.get(patronAccountList.size() - 1);
    }

    @Test
    @Transactional
    void patchNonExistingPatronAccount() throws Exception {
        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        patronAccount.setCardNumber(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPatronAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, patronAccount.getCardNumber())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(patronAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPatronAccount() throws Exception {
        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        patronAccount.setCardNumber(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatronAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(patronAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPatronAccount() throws Exception {
        int databaseSizeBeforeUpdate = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        patronAccount.setCardNumber(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatronAccountMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(patronAccount))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the PatronAccount in the database
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePatronAccount() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccountRepository.saveAndFlush(patronAccount);
        patronAccountRepository.save(patronAccount);
        patronAccountSearchRepository.save(patronAccount);

        int databaseSizeBeforeDelete = patronAccountRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the patronAccount
        restPatronAccountMockMvc
            .perform(delete(ENTITY_API_URL_ID, patronAccount.getCardNumber()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PatronAccount> patronAccountList = patronAccountRepository.findAll();
        assertThat(patronAccountList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(patronAccountSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPatronAccount() throws Exception {
        // Initialize the database
        patronAccount.setCardNumber(UUID.randomUUID().toString());
        patronAccount = patronAccountRepository.saveAndFlush(patronAccount);
        patronAccountSearchRepository.save(patronAccount);

        // Search the patronAccount
        restPatronAccountMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + patronAccount.getCardNumber()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].cardNumber").value(hasItem(patronAccount.getCardNumber())));
    }
}
