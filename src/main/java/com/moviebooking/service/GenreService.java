package com.moviebooking.service;

import com.moviebooking.entity.Genre;
import com.moviebooking.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    // 1. Đổi tên từ getAllGenres thành findAll
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    // 2. Đổi tên từ saveGenre thành save
    public Genre save(Genre genre) {
        return genreRepository.save(genre);
    }

    // 3. Thêm phương thức update
    public Genre update(Long id, Genre genreDetails) {
        // Kiểm tra xem Genre có tồn tại không trước khi cập nhật
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));

        // Cập nhật tên (giả sử Genre của bạn có field name)
        genre.setName(genreDetails.getName());

        // Bạn có thể cập nhật thêm các field khác của Genre tại đây nếu có

        return genreRepository.save(genre);
    }

    // 4. Thêm phương thức delete
    public void delete(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new RuntimeException("Genre not found with id: " + id);
        }
        genreRepository.deleteById(id);
    }
}