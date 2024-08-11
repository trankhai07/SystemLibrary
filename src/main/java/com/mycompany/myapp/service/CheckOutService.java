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
        if (roles.contains(AuthoritiesConstants.USER) || roles.contains(AuthoritiesConstants.ADMIN)) {
            checkOut.setStatus(Status.Pending);
            checkOut.setIsReturned(false);
        }
        if (!checkOut.getStatus().equals(Status.Refused) && checkOut.getEndTime().compareTo(Instant.now()) <= 0) {
            throw new BadRequestException("End time is less than current time!");
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
            Optional<CheckOut> checkOutOld = checkOutRepository.findById(checkOut.getId());

            if (
                !checkOutOld.get().getStatus().equals(Status.Confirmed) &&
                checkOut.getStatus().equals(Status.Confirmed) &&
                checkOut.getEndTime().compareTo(Instant.now()) <= 0
            ) {
                throw new BadRequestException("End time is less than current time!");
            }
            Optional<BookCopy> bookCopy = bookCopyRepository.findById(checkOut.getBookCopy().getId());
            if (roles.contains(AuthoritiesConstants.ADMIN) && checkOut.getStatus().equals(Status.Confirmed)) {
                if (!checkOut.getIsReturned()) {
                    checkOut.setStartTime(Instant.now());
                    if (checkOut.getBookCopy().getAmount() <= 0) throw new BadRequestException("The book is no longer available!");
                    bookCopy.get().setAmount(bookCopy.get().getAmount() - 1);
                    checkOut.setBookCopy(bookCopyRepository.save(bookCopy.get()));
                    checkOutRedisService.saveCheckOutByPatron(checkOut);
                } else checkOutRedisService.deleteCheckOutByKey(checkOut);
            }
            if (checkOut.getIsReturned()) {
                bookCopy.get().setAmount(bookCopy.get().getAmount() + 1);
                if (bookCopyRepository.checkBookAvailable(bookCopy.get().getBook().getId()).isEmpty()) waitListService.Notification(
                    bookCopy.get().getBook().getId()
                );
                bookCopyRepository.save(bookCopy.get());
            }
            CheckOut result = checkOutRepository.save(checkOut);
            checkOutSearchRepository.index(result);
            return result;
        } catch (Exception e) {
            checkOutRedisService.deleteCheckOutByKey(checkOut);
            throw new BadRequestException(e.getMessage());
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
    public Page<CheckOut> findCheckoutByStatus(Status status, Pageable pageable) {
        log.debug("Request to get all CheckOuts by Status");
        return checkOutRepository.findCheckOutByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<CheckOut> findCheckOutByStatusAndReturn(String cardNumber, Status status, boolean returned) {
        log.debug("Request to get all CheckOuts by Status");
        return checkOutRepository.findCheckOutByStatusAndReturn(cardNumber, status, returned);
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
