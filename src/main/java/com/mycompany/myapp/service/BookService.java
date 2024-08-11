package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Author;
import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.BookCopy;
import com.mycompany.myapp.domain.WaitList;
import com.mycompany.myapp.repository.AuthorRepository;
import com.mycompany.myapp.repository.BookCopyRepository;
import com.mycompany.myapp.repository.BookRepository;
import com.mycompany.myapp.repository.WaitListRepository;
import com.mycompany.myapp.repository.search.BookSearchRepository;
import com.mycompany.myapp.service.redis.BookRedisService;
import io.undertow.util.BadRequestException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Book}.
 */
@Service
@Transactional
public class BookService {

    private final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final BookCopyService bookCopyService;
    private final AuthorRepository authorRepository;
    private final BookSearchRepository bookSearchRepository;
    private final BookRedisService bookRedisService;
    private final WaitListRepository waitListRepository;

    public BookService(
        BookRepository bookRepository,
        BookCopyRepository bookCopyRepository,
        BookCopyService bookCopyService,
        AuthorRepository authorRepository,
        BookSearchRepository bookSearchRepository,
        BookRedisService bookRedisService,
        WaitListRepository waitListRepository
    ) {
        this.bookRepository = bookRepository;
        this.bookCopyService = bookCopyService;
        this.authorRepository = authorRepository;
        this.bookSearchRepository = bookSearchRepository;
        this.bookRedisService = bookRedisService;
        this.waitListRepository = waitListRepository;
    }

    /**
     * Save a book.
     *
     * @param book the entity to save.
     * @return the persisted entity.
     */
    public Book save(Book book) throws BadRequestException {
        log.debug("Request to save Book : {}", book);
        try {
            Set<Author> authorSet = new HashSet<>();
            for (Author author : book.getAuthors()) {
                authorSet.add(authorRepository.findById(author.getId()).get());
            }
            book.setAuthors(authorSet);
            Book result = bookRepository.save(book);
            bookSearchRepository.index(result);
            bookRedisService.deleteBooksByCategoryId(book.getCategory().getId());
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Save not successfully!!!");
        }
    }

    /**
     * Update a book.
     *
     * @param book the entity to save.
     * @return the persisted entity.
     */
    public Book update(Book book) throws BadRequestException {
        log.debug("Request to update Book : {}", book);
        try {
            Optional<Book> resultCheck = bookRepository.findById(book.getId());
            String categoryIdUpdate = book.getCategory().getId().toString();
            String categoryIdOld = resultCheck.get().getCategory().getId().toString();
            Book result = bookRepository.save(book);
            bookSearchRepository.index(result);
            if (!categoryIdOld.equals(categoryIdUpdate)) {
                bookRedisService.deleteBooksByCategoryId(Long.parseLong(categoryIdOld));
            }
            bookRedisService.deleteBooksByCategoryId(Long.parseLong(categoryIdUpdate));
            if (bookRedisService.keyBookIdExists(book.getId())) {
                bookRedisService.saveBooks(book);
            }
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Update not successfully!");
        }
    }

    /**
     * Partially update a book.
     *
     * @param book the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Book> partialUpdate(Book book) {
        log.debug("Request to partially update Book : {}", book);

        return bookRepository
            .findById(book.getId())
            .map(existingBook -> {
                if (book.getTitle() != null) {
                    existingBook.setTitle(book.getTitle());
                }
                if (book.getImage() != null) {
                    existingBook.setImage(book.getImage());
                }
                if (book.getDescription() != null) {
                    existingBook.setDescription(book.getDescription());
                }

                return existingBook;
            })
            .map(bookRepository::save)
            .map(savedBook -> {
                bookSearchRepository.save(savedBook);

                return savedBook;
            });
    }

    /**
     * Get all the books.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        log.debug("Request to get all Books");
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Book> findAllByCategoryId(long categoryId, Pageable pageable) {
        log.debug("Request to get all Books");
        if (categoryId == -1) {
            return findAllWithEagerRelationships(pageable);
        }
        if (!bookRedisService.keyExists(categoryId, pageable).isEmpty()) {
            String input = bookRedisService.keyExists(categoryId, pageable).iterator().next();
            String[] parts = input.split(":");
            String lastValueStr = parts[parts.length - 1];
            long total = Integer.parseInt(lastValueStr);
            System.out.println("List book by category redis: " + total);
            return new PageImpl<>(bookRedisService.getBooksByCategoryId(categoryId, total, pageable), pageable, total);
        }
        Page<Long> bookIdsPage = bookRepository.findBookIdsByCategoryId(categoryId, pageable);
        List<Book> books = bookRepository.findAllByIdWithAssociations(bookIdsPage.getContent());
        Page<Book> bookPages = new PageImpl<>(books, pageable, bookIdsPage.getTotalElements());
        if (!books.isEmpty()) {
            bookRedisService.saveBooksByCategoryId(categoryId, bookIdsPage.getTotalElements(), pageable, books);
        }
        return bookPages;
    }

    /**
     * Get all the books with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<Book> findAllWithEagerRelationships(Pageable pageable) {
        return bookRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Get one book by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Book> findOne(Long id) {
        log.debug("Request to get Book : {}", id);
        if (bookRedisService.keyBookIdExists(id)) {
            System.out.println("Book detail redis");
            return Optional.ofNullable(bookRedisService.getBookById(id));
        }
        Optional<Book> book = bookRepository.findOne(id);
        book.ifPresent(bookRedisService::saveBooks);
        return book;
    }

    /**
     * Delete the book by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) throws BadRequestException {
        log.debug("Request to delete Book : {}", id);
        try {
            Optional<Book> resultCheck = bookRepository.findById(id);
            List<BookCopy> bookCopyList = bookCopyService.findAllByBook(id, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            List<WaitList> waitLists = waitListRepository.findByBookId(id);
            for (BookCopy bookCopy : bookCopyList) bookCopyService.delete(bookCopy.getId());

            for (WaitList waitList : waitLists) waitListRepository.deleteById(waitList.getId());

            String categoryIdOld = resultCheck.get().getCategory().getId().toString();
            bookRepository.deleteById(id);
            bookSearchRepository.deleteById(id);
            bookRedisService.deleteBooksByCategoryId(Long.parseLong(categoryIdOld));
            bookRedisService.deleteBooksId(id);
        } catch (Exception e) {
            throw new BadRequestException("Delete not successfully!");
        }
    }

    /**
     * Search for the book corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Book> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Books for query {}", query);
        return bookSearchRepository.search(query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Book> searchByCategory(String query, long categoryId, Pageable pageable) {
        log.debug("Request to search for a page of Books for query {}", query);
        return bookSearchRepository.searchByCategory(query, categoryId, pageable);
    }
}
