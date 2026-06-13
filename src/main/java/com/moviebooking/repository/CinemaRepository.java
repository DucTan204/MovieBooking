package com.moviebooking.repository;

import com.moviebooking.entity.Cinema;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    // Ghi đè phương thức findAll để nạp sẵn danh sách rooms
    @Override
    @EntityGraph(attributePaths = {"rooms"})
    List<Cinema> findAll();
}