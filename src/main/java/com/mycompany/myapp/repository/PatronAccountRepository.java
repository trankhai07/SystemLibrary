package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.PatronAccount;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PatronAccount entity.
 */
@Repository
public interface PatronAccountRepository extends JpaRepository<PatronAccount, String> {
    default Optional<PatronAccount> findOneWithEagerRelationships(String cardNumber) {
        return this.findOneWithToOneRelationships(cardNumber);
    }

    default List<PatronAccount> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<PatronAccount> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct patronAccount from PatronAccount patronAccount left join fetch patronAccount.user",
        countQuery = "select count(distinct patronAccount) from PatronAccount patronAccount"
    )
    Page<PatronAccount> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct patronAccount from PatronAccount patronAccount left join fetch patronAccount.user")
    List<PatronAccount> findAllWithToOneRelationships();

    @Query("select patronAccount from PatronAccount patronAccount left join fetch patronAccount.user where patronAccount.id =:id")
    Optional<PatronAccount> findOneWithToOneRelationships(@Param("id") String id);

    @Query(
        "select distinct pa from PatronAccount pa " +
        "left join fetch pa.user u " +
        "left join fetch u.authorities " +
        "where u.login = :username"
    )
    Optional<PatronAccount> findOneByUserLogin(@Param("username") String username);

    @Query(
        value = "select distinct pa from PatronAccount pa left join fetch pa.user u where 'ROLE_USER' member of u.authorities",
        countQuery = "select count(distinct pa) from PatronAccount pa left join pa.user u where 'ROLE_USER' member of u.authorities"
    )
    Page<PatronAccount> findAllUser(Pageable pageable);

    @Query(
        value = "select distinct pa.*, co.end_time from check_out co " +
        "JOIN patron_account pa ON pa.card_number = co.patron_account_card_number " +
        "and co.end_time < ?1 and co.status = 'Confirmed' " +
        "and co.is_returned = false " +
        "JOIN jhi_user ON jhi_user.id = pa.user_id " +
        "and jhi_user.activated = true order by co.end_time",
        nativeQuery = true
    )
    Page<PatronAccount> listPatronNotEnoughCondition(Instant TimeNow, Pageable pageable);
}
