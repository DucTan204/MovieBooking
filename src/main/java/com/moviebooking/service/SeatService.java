package com.moviebooking.service;

import com.moviebooking.dto.SeatDTO;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Showtime;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.SeatRepository;
import com.moviebooking.repository.ShowtimeRepository;
import com.moviebooking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    private SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setRoomId(seat.getRoom().getId());
        dto.setSeatRow(seat.getSeatRow());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setType(seat.getType().toString());
        return dto;
    }

    // ✅ Bây giờ hàm này sẽ không còn báo lỗi "cannot find symbol" nữa
    public List<SeatDTO> findByRoomId(Long roomId) {
        return seatRepository.findByRoomId(roomId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy ghế kèm trạng thái đã đặt (booked) cho suất chiếu
     */
    public List<SeatDTO> findByShowtimeId(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy suất chiếu ID: " + showtimeId));

        // Lấy danh sách ID ghế đã được bán cho suất chiếu này
        Set<Long> bookedSeatIds = ticketRepository
                .findByBooking_Showtime_Id(showtimeId)
                .stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        // Lấy tất cả ghế của phòng đó và đánh dấu ghế nào đã bán
        return seatRepository.findByRoomId(showtime.getRoom().getId())
                .stream()
                .map(seat -> {
                    SeatDTO dto = toDTO(seat);
                    dto.setBooked(bookedSeatIds.contains(seat.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public SeatDTO save(Seat seat) {
        return toDTO(seatRepository.save(seat));
    }

    public SeatDTO update(Long id, Seat seatDetails) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ghế ID: " + id));

        seat.setSeatRow(seatDetails.getSeatRow());
        seat.setSeatNumber(seatDetails.getSeatNumber());
        seat.setType(seatDetails.getType());

        return toDTO(seatRepository.save(seat));
    }

    public void delete(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy ghế ID: " + id);
        }
        seatRepository.deleteById(id);
    }
}