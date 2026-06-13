package com.moviebooking.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private Long duration;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl; // Thêm
    private String status;     // Thêm
    private List<GenreDTO> genres;
    private List<Long> genreIds;
}
