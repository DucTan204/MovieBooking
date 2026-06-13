package com.moviebooking.dto;

import lombok.Data;
import java.util.List;

@Data
public class CinemaDTO {
    private Long id;
    private String name;
    private String address;
    private String status;      // ← thêm dòng này
    private List<RoomDTO> rooms;
}