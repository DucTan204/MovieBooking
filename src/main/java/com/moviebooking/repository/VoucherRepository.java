package com.moviebooking.repository;

import com.moviebooking.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCodeAndIsActiveTrue(String code);

    List<Voucher> findByIsActiveTrueAndEndDateAfter(LocalDateTime now);
}