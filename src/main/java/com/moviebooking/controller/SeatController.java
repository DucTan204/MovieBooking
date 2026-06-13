package com.moviebooking.controller;

import com.moviebooking.dto.SeatDTO;
import com.moviebooking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    /**
     * Lấy ghế theo phòng (dùng cho Admin quản lý).
     * Không kèm trạng thái booked.
     * GET /api/seats/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(seatService.findByRoomId(roomId));
    }

    /**
     * [API MỚI] Lấy ghế theo suất chiếu, kèm trạng thái booked.
     * Dùng cho trang đặt vé của người dùng.
     * GET /api/seats/showtime/{showtimeId}
     */
    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<List<SeatDTO>> getSeatsByShowtime(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(seatService.findByShowtimeId(showtimeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeat(@PathVariable Long id) {
        seatService.delete(id);
        return ResponseEntity.ok("Seat deleted");
    }
}