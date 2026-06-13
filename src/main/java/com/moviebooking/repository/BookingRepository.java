// BookingRepository.java
package com.moviebooking.repository;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);

    // THÊM DÒNG NÀY: Tìm danh sách đặt vé của User
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
}