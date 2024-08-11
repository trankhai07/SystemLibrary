package com.mycompany.myapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.PatronAccount;
import com.mycompany.myapp.repository.PatronAccountRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.PatronAccountService;
import com.mycompany.myapp.service.dto.AdminUserDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import com.mycompany.myapp.web.rest.vm.ManagedUserVM;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.PatronAccount}.
 */
@RestController
@RequestMapping("/api")
public class PatronAccountResource {

    private final Logger log = LoggerFactory.getLogger(PatronAccountResource.class);

    private static final String ENTITY_NAME = "patronAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PatronAccountService patronAccountService;

    private final PatronAccountRepository patronAccountRepository;

    public PatronAccountResource(PatronAccountService patronAccountService, PatronAccountRepository patronAccountRepository) {
        this.patronAccountService = patronAccountService;
        this.patronAccountRepository = patronAccountRepository;
    }

    /**
     * {@code POST  /patron-accounts} : Create a new patronAccount.
     *
     * @param patronAccount the patronAccount to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new patronAccount, or with status {@code 400 (Bad Request)} if the patronAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/patron-accounts")
    public ResponseEntity<PatronAccount> createPatronAccount(@Valid @RequestBody ManagedUserVM managedUserVM) throws URISyntaxException {
        log.debug("REST request to save PatronAccount : {}", managedUserVM);
        PatronAccount result = patronAccountService.save(managedUserVM, managedUserVM.getPassword());
        return ResponseEntity
            .created(new URI("/api/patron-accounts/" + result.getCardNumber()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getCardNumber()))
            .body(result);
    }

    /**
     * {@code PUT  /patron-accounts/:cardNumber} : Updates an existing patronAccount.
     *
     * @param cardNumber    the id of the patronAccount to save.
     * @param patronAccount the patronAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated patronAccount,
     * or with status {@code 400 (Bad Request)} if the patronAccount is not valid,
     * or with status {@code 500 (Internal Server Error)} if the patronAccount couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/patron-accounts/{cardNumber}")
    public ResponseEntity<PatronAccount> updatePatronAccount(
        @PathVariable(value = "cardNumber", required = false) final String cardNumber,
        @Valid @RequestBody AdminUserDTO userDTO
    ) {
        if (!patronAccountRepository.existsById(cardNumber)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<PatronAccount> result = patronAccountService.update(cardNumber, userDTO);
        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createAlert(applicationName, "patronAccount.updated", userDTO.getLogin()));
    }

    @PutMapping("/patron-accounts-status/{cardNumber}")
    public ResponseEntity<PatronAccount> updatePatronAccountStatus(
        @PathVariable(value = "cardNumber", required = false) final String cardNumber,
        @RequestParam(value = "activated", required = false) Boolean activated
    ) {
        if (!patronAccountRepository.existsById(cardNumber)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<PatronAccount> result = patronAccountService.updateStatus(cardNumber, activated);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createAlert(applicationName, "patronAccount.updated", result.get().getCardNumber())
        );
    }

    /**
     * {@code PATCH  /patron-accounts/:cardNumber} : Partial updates given fields of an existing patronAccount, field will ignore if it is null
     *
     * @param cardNumber    the id of the patronAccount to save.
     * @param patronAccount the patronAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated patronAccount,
     * or with status {@code 400 (Bad Request)} if the patronAccount is not valid,
     * or with status {@code 404 (Not Found)} if the patronAccount is not found,
     * or with status {@code 500 (Internal Server Error)} if the patronAccount couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/patron-accounts/{cardNumber}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PatronAccount> partialUpdatePatronAccount(
        @PathVariable(value = "cardNumber", required = false) final String cardNumber,
        @NotNull @RequestBody PatronAccount patronAccount
    ) throws URISyntaxException {
        log.debug("REST request to partial update PatronAccount partially : {}, {}", cardNumber, patronAccount);
        if (patronAccount.getCardNumber() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(cardNumber, patronAccount.getCardNumber())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!patronAccountRepository.existsById(cardNumber)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PatronAccount> result = patronAccountService.partialUpdate(patronAccount);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, patronAccount.getCardNumber())
        );
    }

    /**
     * {@code GET  /patron-accounts} : get all the patronAccounts.
     *
     * @param pageable  the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of patronAccounts in body.
     */
    @GetMapping("/patron-accounts")
    public ResponseEntity<List<PatronAccount>> getAllPatronAccounts(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of PatronAccounts");
        Page<PatronAccount> page = patronAccountService.findAllUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/patron-accounts/not-enough-condition")
    public ResponseEntity<List<PatronAccount>> getAllPatronAccountsNotEnoughCondition(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get a page of PatronAccountsNotEnoughCondition");
        Page<PatronAccount> page = patronAccountService.listPatronNotEnoughCondition(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /patron-accounts/:id} : get the "id" patronAccount.
     *
     * @param id the id of the patronAccount to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the patronAccount, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/patron-accounts/{id}")
    public ResponseEntity<PatronAccount> getPatronAccount(@PathVariable String id) {
        log.debug("REST request to get PatronAccount : {}", id);
        Optional<PatronAccount> patronAccount = patronAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(patronAccount);
    }

    @GetMapping("/patron-accounts/user")
    public ResponseEntity<PatronAccount> getPatronAccountByUserLogin() {
        Optional<String> username = SecurityUtils.getCurrentUserLogin();
        log.debug("REST request to get PatronAccount : {}", username.get());
        Optional<PatronAccount> patronAccount = patronAccountService.findOneByUserLogin(username.get());
        return ResponseUtil.wrapOrNotFound(patronAccount);
    }

    /**
     * {@code DELETE  /patron-accounts/:id} : delete the "id" patronAccount.
     *
     * @param id the id of the patronAccount to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/patron-accounts/{id}")
    public ResponseEntity<Void> deletePatronAccount(@PathVariable String id) {
        log.debug("REST request to delete PatronAccount : {}", id);
        patronAccountService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /_search/patron-accounts?query=:query} : search for the patronAccount corresponding
     * to the query.
     *
     * @param query    the query of the patronAccount search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/patron-accounts")
    public ResponseEntity<List<PatronAccount>> searchPatronAccounts(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of PatronAccounts for query {}", query);
        Page<PatronAccount> page = patronAccountService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
