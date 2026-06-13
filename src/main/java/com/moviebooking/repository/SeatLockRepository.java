package com.moviebooking.repository;

import com.moviebooking.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    @Modifying
    @Transactional
    void deleteByLockedUntilBefore(LocalDateTime now);

    boolean existsByShowtimeIdAndSeatIdIn(Long showtimeId, List<Long> seatIds);

    @Modifying
    @Transactional
    void deleteByShowtimeIdAndSeatIdIn(Long showtimeId, List<Long> seatIds);
}