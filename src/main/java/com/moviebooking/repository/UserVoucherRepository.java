package com.moviebooking.repository;

import com.moviebooking.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);

    long countByVoucherId(Long voucherId);
}