package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.WaitList;
import com.mycompany.myapp.repository.WaitListRepository;
import com.mycompany.myapp.repository.search.WaitListSearchRepository;
import com.mycompany.myapp.service.dto.InfoCheckOut;
import com.mycompany.myapp.service.redis.WaitListRedisService;
import io.undertow.util.BadRequestException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link WaitList}.
 */
@Service
@Transactional
public class WaitListService {

    private final Logger log = LoggerFactory.getLogger(WaitListService.class);

    private final WaitListRepository waitListRepository;

    private final WaitListSearchRepository waitListSearchRepository;
    private final WaitListRedisService waitListRedisService;
    private final MailService mailService;

    public WaitListService(
        WaitListRepository waitListRepository,
        WaitListSearchRepository waitListSearchRepository,
        WaitListRedisService waitListRedisService,
        MailService mailService
    ) {
        this.waitListRepository = waitListRepository;
        this.waitListSearchRepository = waitListSearchRepository;
        this.waitListRedisService = waitListRedisService;
        this.mailService = mailService;
    }

    /**
     * Save a waitList.
     *
     * @param waitList the entity to save.
     * @return the persisted entity.
     */
    public WaitList save(WaitList waitList) throws BadRequestException {
        log.debug("Request to save WaitList : {}", waitList);
        try {
            WaitList result = waitListRepository.save(waitList);
            waitList.setCreatAt(Instant.now());
            if (result.getBook() != null && result.getPatronAccount() != null) waitListRedisService.add(
                result.getBook(),
                result.getPatronAccount()
            );
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Save wait list not successfully!");
        }
    }

    /**
     * Update a waitList.
     *
     * @param waitList the entity to save.
     * @return the persisted entity.
     */
    public WaitList update(WaitList waitList) {
        log.debug("Request to update WaitList : {}", waitList);
        WaitList result = waitListRepository.save(waitList);
        return result;
    }

    public void Notification(long bookId) {
        List<InfoCheckOut> infoCheckOutList = waitListRedisService.getAll(bookId);
        System.out.println("Notification running");
        if (!infoCheckOutList.isEmpty()) {
            for (InfoCheckOut infoCheckOut : infoCheckOutList) {
                System.out.println(infoCheckOut.getEmail());
                mailService.sendReturnBook(infoCheckOut);
            }
            waitListRedisService.delete(bookId);
        }
    }

    /**
     * Partially update a waitList.
     *
     * @param waitList the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<WaitList> partialUpdate(WaitList waitList) {
        log.debug("Request to partially update WaitList : {}", waitList);

        return waitListRepository
            .findById(waitList.getId())
            .map(existingWaitList -> {
                if (waitList.getCreatAt() != null) {
                    existingWaitList.setCreatAt(waitList.getCreatAt());
                }

                return existingWaitList;
            })
            .map(waitListRepository::save)
            .map(savedWaitList -> {
                waitListSearchRepository.save(savedWaitList);

                return savedWaitList;
            });
    }

    /**
     * Get all the waitLists.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<WaitList> findAll(Pageable pageable) {
        log.debug("Request to get all WaitLists");
        return waitListRepository.findAll(pageable);
    }

    /**
     * Get all the waitLists with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<WaitList> findAllWithEagerRelationships(Pageable pageable) {
        return waitListRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Get one waitList by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<WaitList> findOne(Long id) {
        log.debug("Request to get WaitList : {}", id);
        return waitListRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the waitList by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete WaitList : {}", id);
        waitListRepository.deleteById(id);
        waitListSearchRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Request to delete all table WaitList");
        waitListRepository.deleteAll();
    }

    /**
     * Search for the waitList corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<WaitList> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of WaitLists for query {}", query);
        return waitListSearchRepository.search(query, pageable);
    }
}
