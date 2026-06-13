package com.moviebooking.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOSResponse {
    private String checkoutUrl;      // URL chuyển hướng người dùng đến PayOS
    private String paymentLinkId;    // ID link thanh toán
    private Long orderCode;          // Mã đơn hàng
    private String status;           // PENDING, PAID, CANCELLED, EXPIRED
}