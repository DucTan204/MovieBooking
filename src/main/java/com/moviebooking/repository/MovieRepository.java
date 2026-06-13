package com.moviebooking.repository;

import com.moviebooking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Lấy tất cả phim kèm thể loại - Tối ưu nhất cho trang danh sách phim
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN FETCH m.movieGenres mg " +
            "LEFT JOIN FETCH mg.genre " +
            "ORDER BY m.id DESC")
    List<Movie> findAllWithGenres();

    // Lấy chi tiết 1 bộ phim kèm thể loại - Tối ưu cho trang chi tiết phim
    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.movieGenres mg " +
            "LEFT JOIN FETCH mg.genre " +
            "WHERE m.id = :id")
    Movie findByIdWithGenres(Long id);
}