package com.moviebooking.controller;

import com.moviebooking.dto.SeatLockDTO;
import com.moviebooking.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seat-locks")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    @PostMapping
    public ResponseEntity<?> lockSeats(@RequestBody SeatLockDTO lockDTO) {
        boolean result = seatLockService.lockSeats(lockDTO);
        return ResponseEntity.ok(result);
    }

    // ✅ unlock theo ID lock - Đã sửa Integer -> Long
    @DeleteMapping("/{id}")
    public ResponseEntity<?> unlockSeat(@PathVariable Long id) {
        seatLockService.unlock(id);
        return ResponseEntity.ok("Seat lock released");
    }

    // ✅ unlock nhiều ghế theo showtime - Đã sửa Integer -> Long
    @DeleteMapping
    public ResponseEntity<?> unlockMultiple(
            @RequestParam Long showtimeId,
            @RequestParam List<Long> seatIds) {

        seatLockService.unlockSeats(showtimeId, seatIds);
        return ResponseEntity.ok("Seats unlocked");
    }
}