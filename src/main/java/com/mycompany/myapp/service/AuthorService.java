package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Author;
import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.repository.AuthorRepository;
import com.mycompany.myapp.repository.BookRepository;
import com.mycompany.myapp.repository.search.AuthorSearchRepository;
import com.mycompany.myapp.repository.search.BookSearchRepository;
import com.mycompany.myapp.service.redis.BookRedisService;
import io.undertow.util.BadRequestException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Author}.
 */
@Service
@Transactional
public class AuthorService {

    private final Logger log = LoggerFactory.getLogger(AuthorService.class);
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final AuthorRepository authorRepository;
    private final BookRedisService bookRedisService;
    private final AuthorSearchRepository authorSearchRepository;
    private final BookSearchRepository bookSearchRepository;

    public AuthorService(
        BookRepository bookRepository,
        BookService bookService,
        AuthorRepository authorRepository,
        BookRedisService bookRedisService,
        AuthorSearchRepository authorSearchRepository,
        BookSearchRepository bookSearchRepository
    ) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.authorRepository = authorRepository;
        this.bookRedisService = bookRedisService;
        this.authorSearchRepository = authorSearchRepository;
        this.bookSearchRepository = bookSearchRepository;
    }

    /**
     * Save a author.
     *
     * @param author the entity to save.
     * @return the persisted entity.
     */
    public Author save(Author author) {
        log.debug("Request to save Author : {}", author);
        Author result = authorRepository.save(author);
        authorSearchRepository.index(result);
        return result;
    }

    /**
     * Update a author.
     *
     * @param author the entity to save.
     * @return the persisted entity.
     */
    public Author update(Author author) throws BadRequestException {
        log.debug("Request to update Author : {}", author);
        try {
            Author result = authorRepository.save(author);
            authorSearchRepository.index(result);
            bookRedisService.deleteByPrefix("Book");
            List<Book> bookList = bookRepository.findAllByAuthorId(author.getId());
            for (Book book : bookList) bookSearchRepository.index(bookRepository.findOneWithEagerRelationships(book.getId()).get());
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Update author not successfully!");
        }
    }

    /**
     * Partially update a author.
     *
     * @param author the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Author> partialUpdate(Author author) {
        log.debug("Request to partially update Author : {}", author);

        return authorRepository
            .findById(author.getId())
            .map(existingAuthor -> {
                if (author.getName() != null) {
                    existingAuthor.setName(author.getName());
                }

                return existingAuthor;
            })
            .map(authorRepository::save)
            .map(savedAuthor -> {
                authorSearchRepository.save(savedAuthor);

                return savedAuthor;
            });
    }

    /**
     * Get all the authors.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Author> findAll(Pageable pageable) {
        log.debug("Request to get all Authors");
        return authorRepository.findAll(pageable);
    }

    /**
     * Get one author by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Author> findOne(Long id) {
        log.debug("Request to get Author : {}", id);
        return authorRepository.findById(id);
    }

    /**
     * Delete the author by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Author : {}", id);
        authorRepository.deleteById(id);
        authorSearchRepository.deleteById(id);
    }

    /**
     * Search for the author corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Author> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Authors for query {}", query);
        return authorSearchRepository.search(query, pageable);
    }
}
