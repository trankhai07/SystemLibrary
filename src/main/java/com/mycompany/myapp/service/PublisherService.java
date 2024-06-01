package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Publisher;
import com.mycompany.myapp.repository.PublisherRepository;
import com.mycompany.myapp.repository.search.PublisherSearchRepository;
import com.mycompany.myapp.service.redis.PublisherRedisService;
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
 * Service Implementation for managing {@link Publisher}.
 */
@Service
@Transactional
public class PublisherService {

    private final Logger log = LoggerFactory.getLogger(PublisherService.class);

    private final PublisherRepository publisherRepository;

    private final PublisherSearchRepository publisherSearchRepository;
    private final PublisherRedisService publisherRedisService;

    public PublisherService(
        PublisherRepository publisherRepository,
        PublisherSearchRepository publisherSearchRepository,
        PublisherRedisService publisherRedisService
    ) {
        this.publisherRepository = publisherRepository;
        this.publisherSearchRepository = publisherSearchRepository;
        this.publisherRedisService = publisherRedisService;
    }

    /**
     * Save a publisher.
     *
     * @param publisher the entity to save.
     * @return the persisted entity.
     */
    public Publisher save(Publisher publisher) throws BadRequestException {
        log.debug("Request to save Publisher : {}", publisher);
        try {
            Publisher result = publisherRepository.save(publisher);
            publisherSearchRepository.index(result);
            publisherRedisService.deletePublisherFromRedis();
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Save publisher not successfully!");
        }
    }

    /**
     * Update a publisher.
     *
     * @param publisher the entity to save.
     * @return the persisted entity.
     */
    public Publisher update(Publisher publisher) throws BadRequestException {
        log.debug("Request to update Publisher : {}", publisher);
        try {
            Publisher result = publisherRepository.save(publisher);
            publisherSearchRepository.index(result);
            publisherRedisService.deletePublisherFromRedis();
            return result;
        } catch (Exception e) {
            throw new BadRequestException("Update publisher not successfully!");
        }
    }

    /**
     * Partially update a publisher.
     *
     * @param publisher the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Publisher> partialUpdate(Publisher publisher) {
        log.debug("Request to partially update Publisher : {}", publisher);

        return publisherRepository
            .findById(publisher.getId())
            .map(existingPublisher -> {
                if (publisher.getName() != null) {
                    existingPublisher.setName(publisher.getName());
                }

                return existingPublisher;
            })
            .map(publisherRepository::save)
            .map(savedPublisher -> {
                publisherSearchRepository.save(savedPublisher);

                return savedPublisher;
            });
    }

    /**
     * Get all the publishers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        log.debug("Request to get all Publishers");
        if (publisherRedisService.keyExists()) {
            System.out.println("List publisher");
            return publisherRedisService.getPublisherFromRedis();
        }
        List<Publisher> publishers = publisherRepository.findAll();
        if (!publishers.isEmpty()) publisherRedisService.savePublisherToRedis(publishers);
        return publishers;
    }

    /**
     * Get one publisher by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Publisher> findOne(Long id) {
        log.debug("Request to get Publisher : {}", id);
        return publisherRepository.findById(id);
    }

    /**
     * Delete the publisher by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) throws BadRequestException {
        log.debug("Request to delete Publisher : {}", id);
        try {
            publisherRepository.deleteById(id);
            publisherSearchRepository.deleteById(id);
            publisherRedisService.deletePublisherFromRedis();
        } catch (Exception e) {
            throw new BadRequestException("Delete publisher not successfully!");
        }
    }

    /**
     * Search for the publisher corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Publisher> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Publishers for query {}", query);
        return publisherSearchRepository.search(query, pageable);
    }
}
