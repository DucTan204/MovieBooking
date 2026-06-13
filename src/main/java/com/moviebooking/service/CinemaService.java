package com.moviebooking.service;

import com.moviebooking.dto.CinemaDTO;
import com.moviebooking.dto.RoomDTO;
import com.moviebooking.entity.Cinema;
import com.moviebooking.entity.Room;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;

    private CinemaDTO toDTO(Cinema cinema) {
        CinemaDTO dto = new CinemaDTO();
        dto.setId(cinema.getId());
        dto.setName(cinema.getName());
        dto.setAddress(cinema.getAddress());
        dto.setStatus(cinema.getStatus() != null ? cinema.getStatus().name() : "ACTIVE");

        if (cinema.getRooms() != null) {
            dto.setRooms(cinema.getRooms().stream().map(room -> {
                RoomDTO rDto = new RoomDTO();
                rDto.setId(room.getId());
                rDto.setName(room.getName());
                rDto.setCapacity(room.getCapacity());
                rDto.setStatus(room.getStatus() != null ? room.getStatus().name() : "ACTIVE");
                return rDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    public List<CinemaDTO> findAll() {
        return cinemaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CinemaDTO findById(Long id) {
        return cinemaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy rạp: " + id));
    }

    @Transactional
    public CinemaDTO save(CinemaDTO dto) {
        Cinema cinema = new Cinema();
        cinema.setName(dto.getName());
        cinema.setAddress(dto.getAddress());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                cinema.setStatus(Cinema.CinemaStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                cinema.setStatus(Cinema.CinemaStatus.ACTIVE);
            }
        } else {
            cinema.setStatus(Cinema.CinemaStatus.ACTIVE);
        }
        return toDTO(cinemaRepository.save(cinema));
    }

    @Transactional
    public CinemaDTO update(Long id, CinemaDTO dto) {
        Cinema existing = cinemaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy rạp: " + id));

        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                existing.setStatus(Cinema.CinemaStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                // Giữ nguyên status cũ nếu giá trị không hợp lệ
            }
        }
        return toDTO(cinemaRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy rạp: " + id));
        try {
            cinemaRepository.delete(cinema);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa rạp vì đã có dữ liệu liên quan (Suất chiếu/Vé)");
        }
    }
}