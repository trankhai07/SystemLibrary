package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.CheckOut;
import com.mycompany.myapp.domain.enumeration.Status;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CheckOut entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {
    @Query(value = "select * from check_out where status=?1", nativeQuery = true)
    List<CheckOut> findCheckoutByStatus(String status);

    @Query(value = "select * from check_out where patron_account_card_number = ?1 order by end_time desc ", nativeQuery = true)
    List<CheckOut> findCheckoutByPatron(String patronNumber);

    @Query(
        value = "select checkout from CheckOut checkout left join fetch checkout.bookCopy left join fetch checkout.patronAccount where checkout.status=:status",
        countQuery = "select count(distinct checkout) from CheckOut checkout where checkout.status=:status"
    )
    Page<CheckOut> findCheckOutByStatus(@Param("status") Status status, Pageable pageable);

    @Query(
        value = "select checkout from CheckOut checkout left join fetch checkout.bookCopy left join fetch checkout.patronAccount where checkout.status=:status and " +
        "checkout.isReturned=:returned and checkout.patronAccount.cardNumber=:cardNumber order by checkout.startTime desc"
    )
    List<CheckOut> findCheckOutByStatusAndReturn(
        @Param("cardNumber") String cardNumber,
        @Param("status") Status status,
        @Param("returned") boolean returned
    );
}
