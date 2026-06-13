package com.moviebooking.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeatLockDTO {
    private Long id;
    private Long showtimeId;
    private Long seatId;
    private List<Long> seatIds; // ✅ sửa Integer -> Long
    private LocalDateTime lockedUntil;
}
