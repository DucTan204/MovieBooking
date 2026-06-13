package com.moviebooking.repository;

import com.moviebooking.entity.Movie;
import com.moviebooking.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

// Đổi MovieGenre.MovieGenreId thành Long ở dòng dưới đây
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {

    @Modifying
    @Transactional
    void deleteByMovie(Movie movie);
}