package com.moviebooking.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoomDTO {
    private Long id;
    private Long cinemaId;
    private String cinemaName;
    private String name;
    private Long capacity;
    private String status;      // ← thêm
    private List<SeatDTO> seats;
}