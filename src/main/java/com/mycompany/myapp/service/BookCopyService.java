package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.BookCopy;
import com.mycompany.myapp.repository.BookCopyRepository;
import com.mycompany.myapp.repository.search.BookCopySearchRepository;
import com.mycompany.myapp.service.redis.BookRedisService;
import io.undertow.util.BadRequestException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link BookCopy}.
 */
@Service
@Transactional
public class BookCopyService {

    private final Logger log = LoggerFactory.getLogger(BookCopyService.class);

    private final BookCopyRepository bookCopyRepository;

    private final BookCopySearchRepository bookCopySearchRepository;
    private final WaitListService waitListService;
    private final BookRedisService bookRedisService;

    public BookCopyService(
        BookCopyRepository bookCopyRepository,
        BookCopySearchRepository bookCopySearchRepository,
        WaitListService waitListService,
        BookRedisService bookRedisService
    ) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookCopySearchRepository = bookCopySearchRepository;
        this.waitListService = waitListService;
        this.bookRedisService = bookRedisService;
    }

    /**
     * Save a bookCopy.
     *
     * @param bookCopy the entity to save.
     * @return the persisted entity.
     */
    public BookCopy save(BookCopy bookCopy) throws BadRequestException {
        try {
            log.debug("Request to save BookCopy : {}", bookCopy);
            if (
                findPublishYearOfPublisher(bookCopy.getPublisher().getId(), bookCopy.getBook().getId(), bookCopy.getYearPublished())
                    .isPresent()
            ) throw new BadRequestException("Publish year existed!");
            if (bookCopyRepository.checkBookAvailable(bookCopy.getBook().getId()).isEmpty()) waitListService.Notification(
                bookCopy.getBook().getId()
            );
            BookCopy result = bookCopyRepository.save(bookCopy);
            bookRedisService.deleteBooksId(bookCopy.getBook().getId());
            bookCopySearchRepository.index(result);
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Save book copy not successfully!");
        }
    }

    /**
     * Update a bookCopy.
     *
     * @param bookCopy the entity to save.
     * @return the persisted entity.
     *
     */

    public BookCopy update(BookCopy bookCopy) throws BadRequestException {
        try {
            log.debug("Request to update BookCopy : {}", bookCopy);
            Optional<BookCopy> bookCopyOld = bookCopyRepository.findById(bookCopy.getId());
            Optional<BookCopy> checkExist = findPublishYearOfPublisher(
                bookCopy.getPublisher().getId(),
                bookCopy.getBook().getId(),
                bookCopy.getYearPublished()
            );
            if (checkExist.isPresent() && !Objects.equals(checkExist.get().getId(), bookCopy.getId())) throw new BadRequestException(
                "Publish year existed!"
            );
            if (
                bookCopyOld.get().getAmount() <= 0 &&
                bookCopy.getAmount() > 0 &&
                bookCopyRepository.checkBookAvailable(bookCopy.getBook().getId()).isEmpty()
            ) waitListService.Notification(bookCopy.getBook().getId());
            BookCopy result = bookCopyRepository.save(bookCopy);
            bookRedisService.deleteBooksId(bookCopy.getBook().getId());
            bookCopySearchRepository.index(result);
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Update book copy not successfully!");
        }
    }

    /**
     * Partially update a bookCopy.
     *
     * @param bookCopy the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BookCopy> partialUpdate(BookCopy bookCopy) {
        log.debug("Request to partially update BookCopy : {}", bookCopy);

        return bookCopyRepository
            .findById(bookCopy.getId())
            .map(existingBookCopy -> {
                if (bookCopy.getYearPublished() != null) {
                    existingBookCopy.setYearPublished(bookCopy.getYearPublished());
                }
                if (bookCopy.getAmount() != null) {
                    existingBookCopy.setAmount(bookCopy.getAmount());
                }
                if (bookCopy.getImage() != null) {
                    existingBookCopy.setImage(bookCopy.getImage());
                }
                if (bookCopy.getDescription() != null) {
                    existingBookCopy.setDescription(bookCopy.getDescription());
                }

                return existingBookCopy;
            })
            .map(bookCopyRepository::save)
            .map(savedBookCopy -> {
                bookCopySearchRepository.save(savedBookCopy);

                return savedBookCopy;
            });
    }

    /**
     * Get all the bookCopies.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<BookCopy> findAll(Pageable pageable) {
        log.debug("Request to get all BookCopies");
        return bookCopyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<BookCopy> findAllByBook(long bookId, Pageable pageable) {
        log.debug("Request to get all BookCopies");
        return bookCopyRepository.findAllByBook(bookId, pageable);
    }

    /**
     * Get all the bookCopies with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<BookCopy> findAllWithEagerRelationships(Pageable pageable) {
        return bookCopyRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Get one bookCopy by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BookCopy> findOne(Long id) {
        log.debug("Request to get BookCopy : {}", id);
        return bookCopyRepository.findOneWithEagerRelationships(id);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> checkBookAvailable(Long bookId) {
        log.debug("Request to get BookCopy : {}", bookId);
        return bookCopyRepository.checkBookAvailable(bookId);
    }

    @Transactional(readOnly = true)
    public Optional<BookCopy> findPublishYearOfPublisher(long publisherId, long bookId, long year) {
        log.debug("Request to get BookCopy by year : {}", year);
        return bookCopyRepository.findPublishYearOfPublisher(publisherId, bookId, year);
    }

    /**
     * Delete the bookCopy by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete BookCopy : {}", id);
        bookCopyRepository.deleteById(id);
        bookCopySearchRepository.deleteById(id);
    }

    /**
     * Search for the bookCopy corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<BookCopy> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of BookCopies for query {}", query);
        return bookCopySearchRepository.search(query, pageable);
    }
}
