package com.moviebooking.repository;

import com.moviebooking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Kiểm tra ghế đã đặt xuyên qua bảng Booking
    boolean existsByBooking_Showtime_IdAndSeat_Id(Long showtimeId, Long seatId);

    // Lấy danh sách vé của một đơn hàng
    List<Ticket> findByBookingId(Long bookingId);

    // THÊM DÒNG NÀY: Lấy danh sách vé của một suất chiếu (Tìm xuyên qua Booking)
    List<Ticket> findByBooking_Showtime_Id(Long showtimeId);
}