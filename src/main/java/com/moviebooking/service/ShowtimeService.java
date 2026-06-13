package com.moviebooking.service;

import com.moviebooking.dto.ShowtimeDTO;
import com.moviebooking.entity.Movie;
import com.moviebooking.entity.Room;
import com.moviebooking.entity.Showtime;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.RoomRepository;
import com.moviebooking.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    // ✅ FIX 2: Đã thêm cinemaId vào hàm toDTO
    private ShowtimeDTO toDTO(Showtime st) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setId(st.getId());
        dto.setMovieId(st.getMovie().getId());
        dto.setMovieTitle(st.getMovie().getTitle());
        dto.setRoomId(st.getRoom().getId());
        dto.setRoomName(st.getRoom().getName());
        dto.setCinemaName(st.getRoom().getCinema().getName());

        // Dòng quan trọng nhất để sửa lỗi giá vé 0đ ở Frontend
        dto.setCinemaId(st.getRoom().getCinema().getId());

        dto.setStartTime(st.getStartTime());
        dto.setEndTime(st.getEndTime());
        dto.setBasePrice(st.getBasePrice());
        return dto;
    }

    // Tìm theo ID
    public ShowtimeDTO findById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID không được để trống");
        Showtime st = showtimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy suất chiếu ID: " + id));
        return toDTO(st);
    }

    // TẠO MỚI
    @Transactional
    public ShowtimeDTO save(ShowtimeDTO dto) {
        if (dto.getMovieId() == null || dto.getRoomId() == null) {
            throw new IllegalArgumentException("Movie ID và Room ID là bắt buộc");
        }

        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim ID: " + dto.getMovieId()));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng ID: " + dto.getRoomId()));

        Showtime st = new Showtime();
        st.setMovie(movie);
        st.setRoom(room);
        st.setStartTime(dto.getStartTime());
        st.setEndTime(dto.getEndTime());
        st.setBasePrice(dto.getBasePrice());

        return toDTO(showtimeRepository.save(st));
    }

    // CẬP NHẬT (Sửa)
    @Transactional
    public ShowtimeDTO update(Long id, ShowtimeDTO dto) {
        if (id == null) throw new IllegalArgumentException("ID cập nhật không được để trống");

        Showtime existing = showtimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy suất chiếu ID: " + id));

        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim ID: " + dto.getMovieId()));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng ID: " + dto.getRoomId()));

        existing.setMovie(movie);
        existing.setRoom(room);
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.setBasePrice(dto.getBasePrice());

        Showtime saved = showtimeRepository.saveAndFlush(existing);
        return toDTO(saved);
    }

    // XÓA
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cần xóa không được để trống");
        if (!showtimeRepository.existsById(id)) {
            throw new NotFoundException("Suất chiếu không tồn tại");
        }
        showtimeRepository.deleteById(id);
    }

    // TÌM KIẾM/LỌC
    public List<ShowtimeDTO> search(Long movieId, Long roomId, LocalDate date) {
        return showtimeRepository.findAll().stream()
                .filter(s -> (movieId == null || s.getMovie().getId().equals(movieId)) &&
                        (roomId == null || s.getRoom().getId().equals(roomId)) &&
                        (date == null || s.getStartTime().toLocalDate().equals(date)))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}