package com.moviebooking.service;

import com.moviebooking.entity.Ticket;
import com.moviebooking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<Ticket> getByBooking(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId);
    }

    public List<Ticket> findByShowtime(Long showtimeId) {
        // SỬA TÊN HÀM Ở ĐÂY: Gọi hàm tìm xuyên qua Booking
        return ticketRepository.findByBooking_Showtime_Id(showtimeId);
    }

    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }
}