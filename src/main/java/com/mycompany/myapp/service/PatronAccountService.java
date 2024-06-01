package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.PatronAccount;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.AuthorityRepository;
import com.mycompany.myapp.repository.PatronAccountRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.repository.search.PatronAccountSearchRepository;
import com.mycompany.myapp.repository.search.UserSearchRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.dto.AdminUserDTO;
import java.time.Instant;
import java.util.*;
import javax.swing.text.html.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Service Implementation for managing {@link PatronAccount}.
 */
@Service
@Transactional
public class PatronAccountService {

    private final Logger log = LoggerFactory.getLogger(PatronAccountService.class);

    private final PatronAccountRepository patronAccountRepository;

    private final PatronAccountSearchRepository patronAccountSearchRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final UserSearchRepository userSearchRepository;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;

    public PatronAccountService(
        PatronAccountRepository patronAccountRepository,
        PatronAccountSearchRepository patronAccountSearchRepository,
        UserRepository userRepository,
        UserService userService,
        PasswordEncoder passwordEncoder,
        UserSearchRepository userSearchRepository,
        AuthorityRepository authorityRepository,
        CacheManager cacheManager
    ) {
        this.patronAccountRepository = patronAccountRepository;
        this.patronAccountSearchRepository = patronAccountSearchRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userSearchRepository = userSearchRepository;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Save a patronAccount.
     *
     * @param patronAccount the entity to save.
     * @return the persisted entity.
     */
    public PatronAccount save(AdminUserDTO userDTO, String password) {
        userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .ifPresent(existingUser -> {
                throw new UsernameAlreadyUsedException();
            });
        userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .ifPresent(existingUser -> {
                throw new EmailAlreadyUsedException();
            });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setCreatedDate(Instant.now());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is active
        newUser.setActivated(true);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        userSearchRepository.save(newUser);
        this.clearUserCaches(newUser);
        log.debug("Created Information for User: {}", newUser);

        PatronAccount result = new PatronAccount();
        result.setUser(newUser);
        Random random = new Random();
        int randomNumber = random.nextInt(999999999) + 1;
        String cardNumber = String.format("%010d", randomNumber);
        while (patronAccountRepository.existsById(cardNumber)) {
            randomNumber = random.nextInt(999999999) + 1;
            cardNumber = String.format("%010d", randomNumber);
        }
        result.setCardNumber(cardNumber);
        patronAccountRepository.save(result);
        patronAccountSearchRepository.index(result);
        return result;
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }

    /**
     * Update a patronAccount.
     *
     * @param patronAccount the entity to save.
     * @return the persisted entity.
     */
    public Optional<PatronAccount> update(String cardNumber, AdminUserDTO userDTO) {
        Optional<PatronAccount> patronAccount = patronAccountRepository.findById(cardNumber);
        if (patronAccount.isPresent() && patronAccount.get().getUser() != null) {
            patronAccount.get().setUser(userService.updateUserPatronAccount(userDTO).get());
        }
        PatronAccount result = patronAccountRepository.save(patronAccount.get());
        patronAccountSearchRepository.index(result);
        return Optional.of(result);
    }

    public Optional<PatronAccount> updateStatus(String cardNumber, Boolean activated) {
        Optional<PatronAccount> patronAccount = patronAccountRepository.findById(cardNumber);
        if (patronAccount.isPresent() && patronAccount.get().getUser() != null) {
            Optional<User> user = userRepository.findById(patronAccount.get().getUser().getId());
            this.clearUserCaches(user.get());
            user.get().setActivated(activated);
            patronAccount.get().setUser(userRepository.save(user.get()));
            userSearchRepository.save(user.get());
            this.clearUserCaches(user.get());
        }
        PatronAccount result = patronAccountRepository.save(patronAccount.get());
        patronAccountSearchRepository.index(result);
        return Optional.of(result);
    }

    /**
     * Partially update a patronAccount.
     *
     * @param patronAccount the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PatronAccount> partialUpdate(PatronAccount patronAccount) {
        log.debug("Request to partially update PatronAccount : {}", patronAccount);

        return patronAccountRepository
            .findById(patronAccount.getCardNumber())
            .map(existingPatronAccount -> {
                return existingPatronAccount;
            })
            .map(patronAccountRepository::save)
            .map(savedPatronAccount -> {
                patronAccountSearchRepository.save(savedPatronAccount);

                return savedPatronAccount;
            });
    }

    /**
     * Get all the patronAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PatronAccount> findAll(Pageable pageable) {
        log.debug("Request to get all PatronAccounts");
        return patronAccountRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<PatronAccount> listPatronNotEnoughCondition() {
        log.debug("Request to get listPatronNotEnoughCondition");
        return patronAccountRepository.listPatronNotEnoughCondition(Instant.now());
    }

    /**
     * Get all the patronAccounts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PatronAccount> findAllWithEagerRelationships(Pageable pageable) {
        return patronAccountRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Get one patronAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PatronAccount> findOne(String id) {
        log.debug("Request to get PatronAccount : {}", id);
        return patronAccountRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the patronAccount by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        log.debug("Request to delete PatronAccount : {}", id);
        patronAccountRepository.deleteById(id);
        patronAccountSearchRepository.deleteById(id);
    }

    /**
     * Search for the patronAccount corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PatronAccount> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of PatronAccounts for query {}", query);
        return patronAccountSearchRepository.search(query, pageable);
    }
}
