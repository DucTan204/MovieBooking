package com.moviebooking.controller;

import com.moviebooking.dto.BookingDTO;
import com.moviebooking.entity.User;
import com.moviebooking.service.AuthService;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController // <<< BẮT BUỘC PHẢI CÓ DÒNG NÀY
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TicketService ticketService;

    // 1. Lấy lịch sử đặt vé của tôi (Static path - Để trên cùng)
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingDTO>> getMyBookings() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(bookingService.findMyBookings(currentUser));
    }

    // 2. Lấy danh sách ghế đã đặt (Static path - Để trên cùng)
    @GetMapping("/booked-seats")
    public ResponseEntity<List<Long>> getBookedSeatIds(@RequestParam Long showtimeId) {
        List<Long> ids = ticketService.findByShowtime(showtimeId)
                .stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }

    // 3. Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        return ResponseEntity.ok(bookingService.create(bookingDTO));
    }

    // 4. Lấy chi tiết đơn hàng theo ID (Dynamic path - Để dưới cùng)
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    // 5. Admin lấy tất cả đơn hàng
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.findAll());
    }
}