package com.moviebooking.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TicketDTO {
    private Long id;
    private Long bookingId;
    private Long seatId;
    private String seatNumber;
    private BigDecimal price;

    // ✅ THÊM DÒNG NÀY
    private String audienceType;
}