package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.BookCopy;
import com.mycompany.myapp.domain.CheckOut;
import com.mycompany.myapp.domain.enumeration.Status;
import com.mycompany.myapp.repository.BookCopyRepository;
import com.mycompany.myapp.repository.CheckOutRepository;
import com.mycompany.myapp.repository.search.CheckOutSearchRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.redis.CheckOutRedisService;
import io.undertow.util.BadRequestException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CheckOut}.
 */
@Service
@Transactional
public class CheckOutService {

    private final Logger log = LoggerFactory.getLogger(CheckOutService.class);

    private final CheckOutRepository checkOutRepository;
    private final CheckOutRedisService checkOutRedisService;
    private final CheckOutSearchRepository checkOutSearchRepository;
    private final BookCopyRepository bookCopyRepository;
    private final WaitListService waitListService;

    public CheckOutService(
        CheckOutRepository checkOutRepository,
        CheckOutRedisService checkOutRedisService,
        CheckOutSearchRepository checkOutSearchRepository,
        BookCopyRepository bookCopyRepository,
        WaitListService waitListService
    ) {
        this.checkOutRepository = checkOutRepository;
        this.checkOutRedisService = checkOutRedisService;
        this.checkOutSearchRepository = checkOutSearchRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.waitListService = waitListService;
    }

    /**
     * Save a checkOut.
     *
     * @param checkOut the entity to save.
     * @return the persisted entity.
     */
    public CheckOut save(CheckOut checkOut) throws BadRequestException {
        log.debug("Request to save CheckOut : {}", checkOut);
        Collection<String> roles = SecurityUtils.getCurrentUserRoles();
        if (checkOut.getStatus().equals(Status.Confirmed) && checkOut.getEndTime().compareTo(Instant.now()) <= 0) {
            throw new BadRequestException("End time is less than current time!");
        }
        if (roles.contains(AuthoritiesConstants.USER)) {
            checkOut.setStatus(Status.Pending);
        }
        checkOut.setStartTime(Instant.now());
        CheckOut result = checkOutRepository.save(checkOut);
        checkOutSearchRepository.index(result);
        return result;
    }

    /**
     * Update a checkOut.
     *
     * @param checkOut the entity to save.
     * @return the persisted entity.
     */
    public CheckOut update(CheckOut checkOut) throws BadRequestException {
        log.debug("Request to update CheckOut : {}", checkOut);
        try {
            Collection<String> roles = SecurityUtils.getCurrentUserRoles();
            if (checkOut.getStatus().equals(Status.Confirmed) && checkOut.getEndTime().compareTo(Instant.now()) <= 0) {
                throw new BadRequestException("End time is less than current time!");
            }
            BookCopy bookCopy = checkOut.getBookCopy();
            if (roles.contains(AuthoritiesConstants.ADMIN) && checkOut.getStatus().equals(Status.Confirmed) && !checkOut.getIsReturned()) {
                checkOut.setStartTime(Instant.now());
                if (checkOut.getBookCopy().getAmount() <= 0) throw new BadRequestException("The book is no longer available!");
                bookCopy.setAmount(bookCopy.getAmount() - 1);
                checkOut.setBookCopy(bookCopyRepository.save(bookCopy));
                checkOutRedisService.saveCheckOutByPatron(checkOut);
            }
            if (checkOut.getIsReturned()) {
                bookCopy.setAmount(bookCopy.getAmount() + 1);
                if (bookCopyRepository.checkBookAvailable(bookCopy.getBook().getId()).isEmpty()) waitListService.Notification(
                    bookCopy.getBook().getId()
                );
                bookCopyRepository.save(bookCopy);
            }
            CheckOut result = checkOutRepository.save(checkOut);
            checkOutSearchRepository.index(result);
            return result;
        } catch (Exception e) {
            checkOutRedisService.deleteCheckOutByKey(checkOut);
            throw new BadRequestException("Update Checkout not successfully!");
        }
    }

    /**
     * Partially update a checkOut.
     *
     * @param checkOut the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CheckOut> partialUpdate(CheckOut checkOut) {
        log.debug("Request to partially update CheckOut : {}", checkOut);

        return checkOutRepository
            .findById(checkOut.getId())
            .map(existingCheckOut -> {
                if (checkOut.getStartTime() != null) {
                    existingCheckOut.setStartTime(checkOut.getStartTime());
                }
                if (checkOut.getEndTime() != null) {
                    existingCheckOut.setEndTime(checkOut.getEndTime());
                }
                if (checkOut.getStatus() != null) {
                    existingCheckOut.setStatus(checkOut.getStatus());
                }
                if (checkOut.getIsReturned() != null) {
                    existingCheckOut.setIsReturned(checkOut.getIsReturned());
                }

                return existingCheckOut;
            })
            .map(checkOutRepository::save)
            .map(savedCheckOut -> {
                checkOutSearchRepository.save(savedCheckOut);

                return savedCheckOut;
            });
    }

    /**
     * Get all the checkOuts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CheckOut> findAll(Pageable pageable) {
        log.debug("Request to get all CheckOuts");
        return checkOutRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<CheckOut> findCheckoutByStatus(String status) {
        log.debug("Request to get all CheckoutByStatus");
        return checkOutRepository.findCheckoutByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<CheckOut> findCheckoutByPatronAccount(String patronNumber) {
        log.debug("Request to get all CheckoutByPatronAccount");
        return checkOutRepository.findCheckoutByPatron(patronNumber);
    }

    /**
     * Get one checkOut by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CheckOut> findOne(Long id) {
        log.debug("Request to get CheckOut : {}", id);
        return checkOutRepository.findById(id);
    }

    /**
     * Delete the checkOut by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete CheckOut : {}", id);
        checkOutRepository.deleteById(id);
        checkOutSearchRepository.deleteById(id);
    }

    /**
     * Search for the checkOut corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CheckOut> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of CheckOuts for query {}", query);
        return checkOutSearchRepository.search(query, pageable);
    }
}
