package com.moviebooking.repository;

import com.moviebooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // ✅ PHẢI CÓ DÒNG NÀY: Để SeatService có thể lấy danh sách ghế theo phòng
    List<Seat> findByRoomId(Long roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Seat s WHERE s.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}