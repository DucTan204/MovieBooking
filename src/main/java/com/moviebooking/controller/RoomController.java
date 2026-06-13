package com.moviebooking.controller;

import com.moviebooking.dto.RoomDTO;
import com.moviebooking.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // ✅ Lấy tất cả phòng (Trả về DTO để có thông tin cinemaName)
    @GetMapping
    public ResponseEntity<List<RoomDTO>> findAll() {
        return ResponseEntity.ok(roomService.findAll());
    }

    // ✅ Lấy phòng theo id (Trả về DTO)
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    // ✅ Tạo phòng mới
    @PostMapping
    public ResponseEntity<RoomDTO> create(@RequestBody com.moviebooking.entity.Room room) {
        return ResponseEntity.ok(roomService.save(room));
    }

    // ✅ Cập nhật phòng
    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> update(@PathVariable Long id,
                                          @RequestBody com.moviebooking.entity.Room room) {
        return ResponseEntity.ok(roomService.update(id, room));
    }

    // ✅ Xoá phòng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.ok("Room deleted successfully");
    }
}

