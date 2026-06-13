package com.moviebooking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_genres")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore // 👈 BẮT BUỘC: Không cho phép quay ngược lại Movie từ đây
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    // Giữ Genre để lấy thông tin thể loại (id, name)
    private Genre genre;
}