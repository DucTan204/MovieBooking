package com.moviebooking.service;

import com.moviebooking.dto.MovieDTO;
import com.moviebooking.dto.GenreDTO;
import com.moviebooking.entity.Movie;
import com.moviebooking.entity.MovieGenre;
import com.moviebooking.entity.Genre;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.GenreRepository;
import com.moviebooking.repository.MovieGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;

    private MovieDTO toDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());

        if (movie.getStatus() != null) {
            dto.setStatus(movie.getStatus().name());
        }

        if (movie.getMovieGenres() != null) {
            dto.setGenreIds(movie.getMovieGenres().stream()
                    .map(mg -> mg.getGenre().getId())
                    .collect(Collectors.toList()));

            dto.setGenres(movie.getMovieGenres().stream()
                    .map(mg -> {
                        GenreDTO gDto = new GenreDTO();
                        gDto.setId(mg.getGenre().getId());
                        gDto.setName(mg.getGenre().getName());
                        return gDto;
                    }).collect(Collectors.toList()));
        }
        return dto;
    }

    public List<MovieDTO> findAll() {
        return movieRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO findById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
        return toDTO(movie);
    }

    @Transactional
    public MovieDTO save(MovieDTO dto) {
        Movie movie = new Movie();
        updateMovieFields(movie, dto);
        Movie savedMovie = movieRepository.save(movie);
        saveMovieGenres(savedMovie, dto.getGenreIds());
        return toDTO(savedMovie);
    }

    @Transactional
    public MovieDTO update(Long id, MovieDTO dto) {
        // 1. Tìm phim
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));

        // 2. Cập nhật các trường cơ bản
        updateMovieFields(movie, dto);

        // 3. XỬ LÝ THỂ LOẠI (Sử dụng orphanRemoval = true trong Entity)
        if (movie.getMovieGenres() != null) {
            movie.getMovieGenres().clear();
        }

        // Đẩy lệnh delete xuống DB ngay để dọn dẹp bảng trung gian
        movieRepository.saveAndFlush(movie);

        // 4. Thêm các thể loại mới
        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            List<Long> uniqueIds = dto.getGenreIds().stream().distinct().toList();
            for (Long gId : uniqueIds) {
                Genre genre = genreRepository.findById(gId).orElse(null);
                if (genre != null) {
                    MovieGenre mg = new MovieGenre();
                    mg.setMovie(movie);
                    mg.setGenre(genre);
                    movieGenreRepository.save(mg);
                }
            }
        }

        return toDTO(movieRepository.save(movie));
    }

    private void updateMovieFields(Movie movie, MovieDTO dto) {
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDuration(dto.getDuration());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());

        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            try {
                movie.setStatus(Movie.MovieStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                movie.setStatus(Movie.MovieStatus.SHOWING);
            }
        }
    }

    private void saveMovieGenres(Movie movie, List<Long> genreIds) {
        if (genreIds != null) {
            for (Long gId : genreIds) {
                Genre genre = genreRepository.findById(gId).orElse(null);
                if (genre != null) {
                    MovieGenre mg = new MovieGenre();
                    mg.setMovie(movie);
                    mg.setGenre(genre);
                    movieGenreRepository.save(mg);
                }
            }
        }
    }

    public void delete(Long id) {
        movieRepository.deleteById(id);
    }
}