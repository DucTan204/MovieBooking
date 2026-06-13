package com.moviebooking.repository;

import com.moviebooking.entity.SeatMapConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SeatMapConfigRepository extends JpaRepository<SeatMapConfig, Long> {

    Optional<SeatMapConfig> findByRoomId(Long roomId);
}