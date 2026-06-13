package com.moviebooking.dto;

import lombok.Data;

@Data
public class GenreDTO {
    private Long id; // Đã sửa Integer -> Long
    private String name;
}