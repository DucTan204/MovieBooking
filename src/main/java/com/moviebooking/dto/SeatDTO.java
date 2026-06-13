package com.moviebooking.dto;

import lombok.Data;

@Data
public class SeatDTO {
    private Long id;
    private Long roomId;
    private String seatRow;     // Tên hàng: A, B, C...
    private String seatNumber;  // Số ghế: A1, A2...
    private String type;        // NORMAL | VIP | COUPLE

    /**
     * [THÊM MỚI] Trạng thái đặt chỗ của ghế cho suất chiếu cụ thể.
     * true  = ghế đã có người đặt (hiển thị xám, không cho click)
     * false = ghế còn trống (mặc định)
     */
    private boolean booked = false;
}