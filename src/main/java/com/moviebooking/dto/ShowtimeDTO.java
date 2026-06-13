package com.moviebooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeDTO {
    private Long id;

    @NotNull(message = "ID phim (movieId) không được để trống")
    private Long movieId;
    private String movieTitle;

    @NotNull(message = "ID phòng (roomId) không được để trống")
    private Long roomId;
    private String roomName;

    // ✅ ĐÃ CÓ (Đảm bảo các dòng này tồn tại để BookingService không báo lỗi)
    private Long cinemaId;
    private String cinemaName;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endTime;

    @NotNull(message = "Giá vé không được để trống")
    @Positive(message = "Giá vé phải lớn hơn 0")
    private BigDecimal basePrice;
}