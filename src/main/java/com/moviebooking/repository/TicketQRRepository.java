package com.moviebooking.repository;

import com.moviebooking.entity.TicketQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TicketQRRepository extends JpaRepository<TicketQR, Long> {

    Optional<TicketQR> findByBookingId(Long bookingId);

    Optional<TicketQR> findByQrCode(String qrCode);
}