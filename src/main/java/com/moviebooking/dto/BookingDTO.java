package com.moviebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private String bookingCode;
    private Long userId;
    private Long showtimeId;

    private BigDecimal totalPrice;
    private BigDecimal voucherDiscount;
    private String status;
    private LocalDateTime createdAt;

    private List<Long> seatIds;
    private String voucherCode;

    /**
     * Danh sách loại khán giả cho từng ghế (VD: ["ADULT", "CHILD"])
     */
    private List<String> audienceTypes;

    /**
     * ✅ THÊM DÒNG NÀY:
     * Loại khán giả chung cho toàn bộ đơn hàng nếu không gửi danh sách list.
     */
    private String audienceType;

    private ShowtimeDTO showtime;
    private List<TicketDTO> tickets;
    private String appliedVoucherCode;
}