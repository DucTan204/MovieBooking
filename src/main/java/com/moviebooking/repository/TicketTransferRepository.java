package com.moviebooking.repository;

import com.moviebooking.entity.TicketTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTransferRepository extends JpaRepository<TicketTransfer, Long> {

    List<TicketTransfer> findByFromUserIdOrderByRequestedAtDesc(Long userId);

    Optional<TicketTransfer> findByBookingId(Long bookingId);

    List<TicketTransfer> findByStatus(TicketTransfer.TransferStatus status);
}