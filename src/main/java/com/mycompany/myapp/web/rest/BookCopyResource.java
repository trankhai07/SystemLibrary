package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.BookCopy;
import com.mycompany.myapp.repository.BookCopyRepository;
import com.mycompany.myapp.service.BookCopyService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import io.undertow.util.BadRequestException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.BookCopy}.
 */
@RestController
@RequestMapping("/api")
public class BookCopyResource {

    private final Logger log = LoggerFactory.getLogger(BookCopyResource.class);

    private static final String ENTITY_NAME = "bookCopy";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BookCopyService bookCopyService;

    private final BookCopyRepository bookCopyRepository;

    public BookCopyResource(BookCopyService bookCopyService, BookCopyRepository bookCopyRepository) {
        this.bookCopyService = bookCopyService;
        this.bookCopyRepository = bookCopyRepository;
    }

    /**
     * {@code POST  /book-copies} : Create a new bookCopy.
     *
     * @param bookCopy the bookCopy to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bookCopy, or with status {@code 400 (Bad Request)} if the bookCopy has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/book-copies")
    public ResponseEntity<BookCopy> createBookCopy(@RequestBody BookCopy bookCopy) throws URISyntaxException, BadRequestException {
        log.debug("REST request to save BookCopy : {}", bookCopy);
        if (bookCopy.getId() != null) {
            throw new BadRequestAlertException("A new bookCopy cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BookCopy result = bookCopyService.save(bookCopy);
        return ResponseEntity
            .created(new URI("/api/book-copies/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /book-copies/:id} : Updates an existing bookCopy.
     *
     * @param id       the id of the bookCopy to save.
     * @param bookCopy the bookCopy to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bookCopy,
     * or with status {@code 400 (Bad Request)} if the bookCopy is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bookCopy couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/book-copies/{id}")
    public ResponseEntity<BookCopy> updateBookCopy(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody BookCopy bookCopy
    ) throws URISyntaxException, BadRequestException {
        log.debug("REST request to update BookCopy : {}, {}", id, bookCopy);
        if (bookCopy.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bookCopy.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bookCopyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        BookCopy result = bookCopyService.update(bookCopy);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, bookCopy.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /book-copies/:id} : Partial updates given fields of an existing bookCopy, field will ignore if it is null
     *
     * @param id       the id of the bookCopy to save.
     * @param bookCopy the bookCopy to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bookCopy,
     * or with status {@code 400 (Bad Request)} if the bookCopy is not valid,
     * or with status {@code 404 (Not Found)} if the bookCopy is not found,
     * or with status {@code 500 (Internal Server Error)} if the bookCopy couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/book-copies/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BookCopy> partialUpdateBookCopy(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody BookCopy bookCopy
    ) throws URISyntaxException {
        log.debug("REST request to partial update BookCopy partially : {}, {}", id, bookCopy);
        if (bookCopy.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bookCopy.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bookCopyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BookCopy> result = bookCopyService.partialUpdate(bookCopy);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, bookCopy.getId().toString())
        );
    }

    /**
     * {@code GET  /book-copies} : get all the bookCopies.
     *
     * @param pageable  the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bookCopies in body.
     */
    @GetMapping("/book-copies")
    public ResponseEntity<List<BookCopy>> getAllBookCopies(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of BookCopies");
        Page<BookCopy> page;
        if (eagerload) {
            page = bookCopyService.findAllWithEagerRelationships(pageable);
        } else {
            page = bookCopyService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/book-copies/check-book-available")
    public ResponseEntity<List<BookCopy>> checkBookAvailable(@RequestParam(value = "bookId", required = false) long bookId) {
        List<BookCopy> bookCopies = bookCopyService.checkBookAvailable(bookId);
        return ResponseEntity.ok().body(bookCopies);
    }

    /**
     * {@code GET  /book-copies/:id} : get the "id" bookCopy.
     *
     * @param id the id of the bookCopy to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bookCopy, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/book-copies/{id}")
    public ResponseEntity<BookCopy> getBookCopy(@PathVariable Long id) {
        log.debug("REST request to get BookCopy : {}", id);
        Optional<BookCopy> bookCopy = bookCopyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(bookCopy);
    }

    @GetMapping("/book-copies/publish-year")
    public BookCopy getBookCopyPublishYear(
        @RequestParam(value = "publisherId", required = false) long publisherId,
        @RequestParam(value = "bookId", required = false) long bookId,
        @RequestParam(value = "year", required = false) long year
    ) {
        log.debug("REST request to get BookCopy by year: {}", year);
        Optional<BookCopy> bookCopy = bookCopyService.findPublishYearOfPublisher(publisherId, bookId, year);
        return bookCopy.orElseGet(BookCopy::new);
    }

    @GetMapping("/book-copies/book")
    public ResponseEntity<List<BookCopy>> getAllBookCopiesByBook(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        @RequestParam(value = "bookId", required = false, defaultValue = "-1") long bookId
    ) {
        log.debug("REST request to get a page of BookCopies");
        if (bookId == -1) {
            return null;
        }
        Page<BookCopy> page = bookCopyService.findAllByBook(bookId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code DELETE  /book-copies/:id} : delete the "id" bookCopy.
     *
     * @param id the id of the bookCopy to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/book-copies/{id}")
    public ResponseEntity<Void> deleteBookCopy(@PathVariable Long id) {
        log.debug("REST request to delete BookCopy : {}", id);
        bookCopyService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/book-copies?query=:query} : search for the bookCopy corresponding
     * to the query.
     *
     * @param query    the query of the bookCopy search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/book-copies")
    public ResponseEntity<List<BookCopy>> searchBookCopies(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of BookCopies for query {}", query);
        Page<BookCopy> page = bookCopyService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
