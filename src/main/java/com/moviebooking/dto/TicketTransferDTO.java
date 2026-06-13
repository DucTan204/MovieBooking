// com/moviebooking/dto/TicketTransferDTO.java
package com.moviebooking.dto;

import com.moviebooking.entity.TicketTransfer;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketTransferDTO {

    private Long id;

    // Thông tin booking
    private Long   bookingId;
    private String bookingCode;
    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private LocalDateTime showtimeStart;

    // Thông tin user
    private Long   fromUserId;
    private String fromUserName;
    private String fromUserEmail;

    // Tiền
    private BigDecimal totalPrice;
    private BigDecimal refundAmount;
    private BigDecimal feeAmount;

    // Thông tin nhận tiền
    private String reason;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    // Trạng thái
    private String status;
    private String rejectReason;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}