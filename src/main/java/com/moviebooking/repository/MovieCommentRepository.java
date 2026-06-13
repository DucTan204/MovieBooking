package com.moviebooking.repository;

import com.moviebooking.entity.MovieComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieCommentRepository extends JpaRepository<MovieComment, Long> {

    List<MovieComment> findByMovieIdAndIsApprovedTrueOrderByCreatedAtDesc(Long movieId);

    List<MovieComment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT AVG(c.rating) FROM MovieComment c WHERE c.movie.id = :movieId AND c.isApproved = true")
    Double findAvgRatingByMovieId(Long movieId);

    long countByMovieIdAndIsApprovedTrue(Long movieId);
}