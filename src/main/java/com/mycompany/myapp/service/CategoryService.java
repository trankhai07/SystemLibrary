package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.repository.BookRepository;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.repository.search.BookSearchRepository;
import com.mycompany.myapp.repository.search.CategorySearchRepository;
import com.mycompany.myapp.service.redis.BookRedisService;
import com.mycompany.myapp.service.redis.CategoryRedisService;
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
 * Service Implementation for managing {@link Category}.
 */
@Service
@Transactional
public class CategoryService {

    private final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final BookSearchRepository bookSearchRepository;
    private final BookRedisService bookRedisService;
    private final CategoryRedisService categoryRedisService;
    private final CategorySearchRepository categorySearchRepository;

    public CategoryService(
        CategoryRepository categoryRepository,
        BookRepository bookRepository,
        BookSearchRepository bookSearchRepository,
        BookRedisService bookRedisService,
        CategoryRedisService categoryRedisService,
        CategorySearchRepository categorySearchRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.bookSearchRepository = bookSearchRepository;
        this.bookRedisService = bookRedisService;
        this.categoryRedisService = categoryRedisService;
        this.categorySearchRepository = categorySearchRepository;
    }

    /**
     * Save a category.
     *
     * @param category the entity to save.
     * @return the persisted entity.
     */
    public Category save(Category category) throws BadRequestException {
        log.debug("Request to save Category : {}", category);
        try {
            Category result = categoryRepository.save(category);
            categorySearchRepository.index(result);
            categoryRedisService.deleteCategoryFromRedis();
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Save category not successfully!");
        }
    }

    /**
     * Update a category.
     *
     * @param category the entity to save.
     * @return the persisted entity.
     */
    public Category update(Category category) throws BadRequestException {
        log.debug("Request to update Category : {}", category);
        try {
            Category result = categoryRepository.save(category);
            categorySearchRepository.index(result);

            List<Book> bookList = bookRepository.findAllByCategoryId(category.getId());
            for (Book book : bookList) {
                bookSearchRepository.index(book);
                if (bookRedisService.keyBookIdExists(book.getId())) bookRedisService.saveBooks(book);
            }
            bookRedisService.deleteBooksByCategoryId(category.getId());
            categoryRedisService.deleteCategoryFromRedis();
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Update category not successfully!");
        }
    }

    /**
     * Partially update a category.
     *
     * @param category the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Category> partialUpdate(Category category) {
        log.debug("Request to partially update Category : {}", category);

        return categoryRepository
            .findById(category.getId())
            .map(existingCategory -> {
                if (category.getName() != null) {
                    existingCategory.setName(category.getName());
                }
                if (category.getDescription() != null) {
                    existingCategory.setDescription(category.getDescription());
                }

                return existingCategory;
            })
            .map(categoryRepository::save)
            .map(savedCategory -> {
                categorySearchRepository.save(savedCategory);

                return savedCategory;
            });
    }

    /**
     * Get all the categories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        log.debug("Request to get all Categories");
        if (categoryRedisService.keyExists()) {
            System.out.println("List category");
            return categoryRedisService.getCategoryFromRedis();
        }
        List<Category> categoryList = categoryRepository.findAll();
        if (!categoryList.isEmpty()) categoryRedisService.saveCategoryToRedis(categoryList);
        return categoryList;
    }

    /**
     * Get one category by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Category> findOne(Long id) {
        log.debug("Request to get Category : {}", id);
        return categoryRepository.findById(id);
    }

    /**
     * Delete the category by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Category : {}", id);
        categoryRepository.deleteById(id);
        categorySearchRepository.deleteById(id);
        categoryRedisService.deleteCategoryFromRedis();
    }

    /**
     * Search for the category corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Category> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Categories for query {}", query);
        return categorySearchRepository.search(query, pageable);
    }
}
