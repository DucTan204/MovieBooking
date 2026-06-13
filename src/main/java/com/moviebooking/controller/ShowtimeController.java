package com.moviebooking.controller;

import com.moviebooking.dto.ShowtimeDTO;
import com.moviebooking.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {
    @Autowired
    private ShowtimeService showtimeService;

    // Lấy danh sách suất chiếu (có lọc)
    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.search(movieId, roomId, date));
    }

    // Lấy 1 suất chiếu theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.findById(id));
    }

    // TẠO MỚI (Admin)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ShowtimeDTO> createShowtime(@Valid @RequestBody ShowtimeDTO showtimeDTO) {
        return ResponseEntity.ok(showtimeService.save(showtimeDTO));
    }

    // CẬP NHẬT (Admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ShowtimeDTO> updateShowtime(@PathVariable Long id, @Valid @RequestBody ShowtimeDTO showtimeDTO) {
        return ResponseEntity.ok(showtimeService.update(id, showtimeDTO));
    }

    // XÓA (Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteShowtime(@PathVariable Long id) {
        showtimeService.delete(id);
        return ResponseEntity.ok("Showtime deleted successfully");
    }
}