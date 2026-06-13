package com.moviebooking.repository;

import com.moviebooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ✅ đúng với entity (cinema là object)
    List<Room> findByCinema_Id(Long cinemaId);
}