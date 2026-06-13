package com.moviebooking.controller;

import com.moviebooking.dto.CinemaDTO; // Đảm bảo import DTO
import com.moviebooking.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    // Lấy tất cả rạp (Phải trả về CinemaDTO)
    @GetMapping
    public ResponseEntity<List<CinemaDTO>> getAllCinemas() {
        return ResponseEntity.ok(cinemaService.findAll());
    }

    // Lấy chi tiết rạp (Sửa lỗi 400 ở trang chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<CinemaDTO> getCinemaById(@PathVariable Long id) {
        // Gọi thẳng findById của Service để trả về DTO
        return ResponseEntity.ok(cinemaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CinemaDTO> createCinema(@RequestBody CinemaDTO cinemaDTO) {
        return ResponseEntity.ok(cinemaService.save(cinemaDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CinemaDTO> updateCinema(@PathVariable Long id, @RequestBody CinemaDTO cinemaDTO) {
        return ResponseEntity.ok(cinemaService.update(id, cinemaDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {
        cinemaService.delete(id);
        return ResponseEntity.ok().build();
    }
}