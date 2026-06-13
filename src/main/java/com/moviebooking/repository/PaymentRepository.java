package com.moviebooking.repository;

import com.moviebooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Thêm dòng này để tìm kiếm thanh toán theo ID đặt vé
    Optional<Payment> findByBookingId(Long bookingId);
}