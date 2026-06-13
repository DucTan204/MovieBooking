package com.moviebooking.service;

import com.moviebooking.dto.RoomDTO;
import com.moviebooking.entity.Room;
import com.moviebooking.entity.Seat;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.RoomRepository;
import com.moviebooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;

    private RoomDTO toDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCapacity(room.getCapacity());
        dto.setStatus(room.getStatus() != null ? room.getStatus().name() : "ACTIVE");
        if (room.getCinema() != null) {
            dto.setCinemaId(room.getCinema().getId());
            dto.setCinemaName(room.getCinema().getName());
        }
        return dto;
    }

    public RoomDTO findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng chiếu với ID: " + id));
        return toDTO(room);
    }

    public List<RoomDTO> findAll() {
        return roomRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDTO save(Room room) {
        // Đảm bảo status mặc định nếu không truyền lên
        if (room.getStatus() == null) {
            room.setStatus(Room.RoomStatus.ACTIVE);
        }

        Room savedRoom = roomRepository.save(room);
        generateSeatsForRoom(savedRoom);
        return toDTO(savedRoom);
    }

    private void generateSeatsForRoom(Room room) {
        int capacity = room.getCapacity().intValue();
        int seatsPerRow = 10;
        List<Seat> seatsToSave = new ArrayList<>();

        for (int i = 0; i < capacity; i++) {
            int rowIdx = i / seatsPerRow;
            int colIdx = (i % seatsPerRow) + 1;

            StringBuilder rowLabel = new StringBuilder();
            int tempIdx = rowIdx;
            while (tempIdx >= 0) {
                rowLabel.insert(0, (char) ('A' + (tempIdx % 26)));
                tempIdx = (tempIdx / 26) - 1;
            }

            Seat seat = new Seat();
            seat.setRoom(room);
            seat.setSeatRow(rowLabel.toString());
            seat.setSeatNumber(rowLabel.toString() + colIdx);
            seat.setType(rowIdx < 3 ? Seat.SeatType.NORMAL : Seat.SeatType.VIP);

            seatsToSave.add(seat);
        }
        seatRepository.saveAll(seatsToSave);
    }

    @Transactional
    public RoomDTO update(Long id, Room roomRequest) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // Nếu sức chứa thay đổi → xoá ghế cũ và tạo lại
        if (!room.getCapacity().equals(roomRequest.getCapacity())) {
            seatRepository.deleteByRoomId(id);
            room.setCapacity(roomRequest.getCapacity());
            Room updatedRoom = roomRepository.save(room);
            generateSeatsForRoom(updatedRoom);
            room = updatedRoom;
        }

        room.setName(roomRequest.getName());

        // Cập nhật status nếu có truyền lên
        if (roomRequest.getStatus() != null) {
            room.setStatus(roomRequest.getStatus());
        }

        if (roomRequest.getCinema() != null) {
            room.setCinema(roomRequest.getCinema());
        }

        return toDTO(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new NotFoundException("Room not found");
        }
        seatRepository.deleteByRoomId(id);
        roomRepository.deleteById(id);
    }
}