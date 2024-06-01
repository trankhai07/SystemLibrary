package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.CheckOut;
import java.util.List;
import org.springframework.data.jpa.repository.*;
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
}
