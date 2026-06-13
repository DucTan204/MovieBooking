package com.moviebooking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String method; // MOMO, PAYOS
    private String status; // PENDING, SUCCESS, FAILED
    private String transactionCode;
    private LocalDateTime createdAt;
}