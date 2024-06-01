package com.mycompany.myapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.CheckOut;
import com.mycompany.myapp.domain.enumeration.Status;
import com.mycompany.myapp.repository.CheckOutRepository;
import com.mycompany.myapp.service.CheckOutService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import io.undertow.util.BadRequestException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.CheckOut}.
 */
@RestController
@RequestMapping("/api")
public class CheckOutResource {

    private final Logger log = LoggerFactory.getLogger(CheckOutResource.class);

    private static final String ENTITY_NAME = "checkOut";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CheckOutService checkOutService;

    private final CheckOutRepository checkOutRepository;

    public CheckOutResource(CheckOutService checkOutService, CheckOutRepository checkOutRepository) {
        this.checkOutService = checkOutService;
        this.checkOutRepository = checkOutRepository;
    }

    /**
     * {@code POST  /check-outs} : Create a new checkOut.
     *
     * @param checkOut the checkOut to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new checkOut, or with status {@code 400 (Bad Request)} if the checkOut has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/check-outs")
    public ResponseEntity<CheckOut> createCheckOut(@RequestBody CheckOut checkOut) throws URISyntaxException, BadRequestException {
        log.debug("REST request to save CheckOut : {}", checkOut);
        if (checkOut.getId() != null) {
            throw new BadRequestAlertException("A new checkOut cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CheckOut result = checkOutService.save(checkOut);
        return ResponseEntity
            .created(new URI("/api/check-outs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /check-outs/:id} : Updates an existing checkOut.
     *
     * @param id       the id of the checkOut to save.
     * @param checkOut the checkOut to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated checkOut,
     * or with status {@code 400 (Bad Request)} if the checkOut is not valid,
     * or with status {@code 500 (Internal Server Error)} if the checkOut couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/check-outs/{id}")
    public ResponseEntity<CheckOut> updateCheckOut(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CheckOut checkOut
    ) throws URISyntaxException, BadRequestException {
        log.debug("REST request to update CheckOut : {}, {}", id, checkOut);
        if (checkOut.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, checkOut.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!checkOutRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CheckOut result = checkOutService.update(checkOut);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, checkOut.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /check-outs/:id} : Partial updates given fields of an existing checkOut, field will ignore if it is null
     *
     * @param id       the id of the checkOut to save.
     * @param checkOut the checkOut to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated checkOut,
     * or with status {@code 400 (Bad Request)} if the checkOut is not valid,
     * or with status {@code 404 (Not Found)} if the checkOut is not found,
     * or with status {@code 500 (Internal Server Error)} if the checkOut couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/check-outs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CheckOut> partialUpdateCheckOut(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CheckOut checkOut
    ) throws URISyntaxException {
        log.debug("REST request to partial update CheckOut partially : {}, {}", id, checkOut);
        if (checkOut.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, checkOut.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!checkOutRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CheckOut> result = checkOutService.partialUpdate(checkOut);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, checkOut.getId().toString())
        );
    }

    /**
     * {@code GET  /check-outs} : get all the checkOuts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of checkOuts in body.
     */
    @GetMapping("/check-outs")
    public ResponseEntity<List<CheckOut>> getAllCheckOuts(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of CheckOuts");
        Page<CheckOut> page = checkOutService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/check-outs/status")
    public ResponseEntity<List<CheckOut>> getAllCheckOutsByStatus(@RequestParam(value = "Status", required = false) Status status) {
        log.debug("REST request to get a page of CheckOuts");
        List<CheckOut> checkOuts = checkOutService.findCheckoutByStatus(status.toString());
        return ResponseEntity.ok().body(checkOuts);
    }

    @GetMapping("/check-outs/patron-account")
    public ResponseEntity<List<CheckOut>> getAllCheckOutsByPatronAccount(
        @RequestParam(value = "patronNumber", required = false) String patronNumber
    ) {
        log.debug("REST request to get a page of CheckOuts");
        List<CheckOut> checkOuts = checkOutService.findCheckoutByPatronAccount(patronNumber.trim());
        return ResponseEntity.ok().body(checkOuts);
    }

    /**
     * {@code GET  /check-outs/:id} : get the "id" checkOut.
     *
     * @param id the id of the checkOut to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the checkOut, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/check-outs/{id}")
    public ResponseEntity<CheckOut> getCheckOut(@PathVariable Long id) {
        log.debug("REST request to get CheckOut : {}", id);
        Optional<CheckOut> checkOut = checkOutService.findOne(id);
        return ResponseUtil.wrapOrNotFound(checkOut);
    }

    /**
     * {@code DELETE  /check-outs/:id} : delete the "id" checkOut.
     *
     * @param id the id of the checkOut to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/check-outs/{id}")
    public ResponseEntity<Void> deleteCheckOut(@PathVariable Long id) {
        log.debug("REST request to delete CheckOut : {}", id);
        checkOutService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/check-outs?query=:query} : search for the checkOut corresponding
     * to the query.
     *
     * @param query    the query of the checkOut search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/check-outs")
    public ResponseEntity<List<CheckOut>> searchCheckOuts(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of CheckOuts for query {}", query);
        Page<CheckOut> page = checkOutService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
