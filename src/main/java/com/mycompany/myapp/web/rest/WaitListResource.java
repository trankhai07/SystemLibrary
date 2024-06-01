package com.mycompany.myapp.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.WaitList;
import com.mycompany.myapp.repository.WaitListRepository;
import com.mycompany.myapp.service.WaitListService;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.WaitList}.
 */
@RestController
@RequestMapping("/api")
public class WaitListResource {

    private final Logger log = LoggerFactory.getLogger(WaitListResource.class);

    private static final String ENTITY_NAME = "waitList";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WaitListService waitListService;

    private final WaitListRepository waitListRepository;

    public WaitListResource(WaitListService waitListService, WaitListRepository waitListRepository) {
        this.waitListService = waitListService;
        this.waitListRepository = waitListRepository;
    }

    /**
     * {@code POST  /wait-lists} : Create a new waitList.
     *
     * @param waitList the waitList to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new waitList, or with status {@code 400 (Bad Request)} if the waitList has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/wait-lists")
    public ResponseEntity<WaitList> createWaitList(@RequestBody WaitList waitList) throws URISyntaxException, BadRequestException {
        log.debug("REST request to save WaitList : {}", waitList);
        if (waitList.getId() != null) {
            throw new BadRequestAlertException("A new waitList cannot already have an ID", ENTITY_NAME, "idexists");
        }
        WaitList result = waitListService.save(waitList);
        return ResponseEntity
            .created(new URI("/api/wait-lists/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /wait-lists/:id} : Updates an existing waitList.
     *
     * @param id       the id of the waitList to save.
     * @param waitList the waitList to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated waitList,
     * or with status {@code 400 (Bad Request)} if the waitList is not valid,
     * or with status {@code 500 (Internal Server Error)} if the waitList couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/wait-lists/{id}")
    public ResponseEntity<WaitList> updateWaitList(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody WaitList waitList
    ) throws URISyntaxException {
        log.debug("REST request to update WaitList : {}, {}", id, waitList);
        if (waitList.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, waitList.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!waitListRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        WaitList result = waitListService.update(waitList);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, waitList.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /wait-lists/:id} : Partial updates given fields of an existing waitList, field will ignore if it is null
     *
     * @param id       the id of the waitList to save.
     * @param waitList the waitList to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated waitList,
     * or with status {@code 400 (Bad Request)} if the waitList is not valid,
     * or with status {@code 404 (Not Found)} if the waitList is not found,
     * or with status {@code 500 (Internal Server Error)} if the waitList couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/wait-lists/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WaitList> partialUpdateWaitList(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody WaitList waitList
    ) throws URISyntaxException {
        log.debug("REST request to partial update WaitList partially : {}, {}", id, waitList);
        if (waitList.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, waitList.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!waitListRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WaitList> result = waitListService.partialUpdate(waitList);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, waitList.getId().toString())
        );
    }

    /**
     * {@code GET  /wait-lists} : get all the waitLists.
     *
     * @param pageable  the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of waitLists in body.
     */
    @GetMapping("/wait-lists")
    public ResponseEntity<List<WaitList>> getAllWaitLists(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of WaitLists");
        Page<WaitList> page;
        if (eagerload) {
            page = waitListService.findAllWithEagerRelationships(pageable);
        } else {
            page = waitListService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /wait-lists/:id} : get the "id" waitList.
     *
     * @param id the id of the waitList to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the waitList, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/wait-lists/{id}")
    public ResponseEntity<WaitList> getWaitList(@PathVariable Long id) {
        log.debug("REST request to get WaitList : {}", id);
        Optional<WaitList> waitList = waitListService.findOne(id);
        return ResponseUtil.wrapOrNotFound(waitList);
    }

    /**
     * {@code DELETE  /wait-lists/:id} : delete the "id" waitList.
     *
     * @param id the id of the waitList to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/wait-lists/{id}")
    public ResponseEntity<Void> deleteWaitList(@PathVariable Long id) {
        log.debug("REST request to delete WaitList : {}", id);
        waitListService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @DeleteMapping("/wait-lists/delete-all")
    public ResponseEntity<Void> deleteWaitListAll() {
        waitListService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code SEARCH  /_search/wait-lists?query=:query} : search for the waitList corresponding
     * to the query.
     *
     * @param query    the query of the waitList search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/wait-lists")
    public ResponseEntity<List<WaitList>> searchWaitLists(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of WaitLists for query {}", query);
        Page<WaitList> page = waitListService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
