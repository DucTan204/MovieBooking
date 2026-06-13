package com.moviebooking.service;

import com.moviebooking.dto.SeatLockDTO;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.SeatLock;
import com.moviebooking.entity.Showtime;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.SeatLockRepository;
import com.moviebooking.repository.SeatRepository;
import com.moviebooking.repository.ShowtimeRepository;
import com.moviebooking.repository.TicketRepository; // Import thêm
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final SeatLockRepository seatLockRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository; // Inject TicketRepository

    @Transactional
    public boolean lockSeats(SeatLockDTO dto) {
        // 1. Kiểm tra xem có ghế nào trong danh sách đã bị khóa (đang chờ thanh toán) chưa
        if (seatLockRepository.existsByShowtimeIdAndSeatIdIn(dto.getShowtimeId(), dto.getSeatIds())) {
            throw new RuntimeException("Một số ghế đã được chọn hoặc đang được người khác giữ. Vui lòng chọn ghế khác!");
        }

        // 2. [QUAN TRỌNG] Kiểm tra xem có ghế nào đã được thanh toán (đã có vé) chưa
        for (Long seatId : dto.getSeatIds()) {
            // Lưu ý: Tên phương thức này phải khớp với khai báo trong TicketRepository của bạn
            boolean isSold = ticketRepository.existsByBooking_Showtime_IdAndSeat_Id(dto.getShowtimeId(), seatId);
            if (isSold) {
                throw new RuntimeException("Một số ghế đã được bán. Vui lòng tải lại trang!");
            }
        }

        // 3. Thực hiện khóa ghế
        Showtime showtime = showtimeRepository.findById(dto.getShowtimeId())
                .orElseThrow(() -> new NotFoundException("Showtime not found"));

        for (Long seatId : dto.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new NotFoundException("Seat not found"));

            SeatLock lock = new SeatLock();
            lock.setShowtime(showtime);
            lock.setSeat(seat);
            lock.setLockedUntil(LocalDateTime.now().plusMinutes(10)); // Khóa trong 10 phút

            seatLockRepository.save(lock);
        }

        return true;
    }

    // ✅ unlock 1 ghế theo lock id
    @Transactional
    public void unlock(Long id) {
        if (!seatLockRepository.existsById(id)) {
            throw new NotFoundException("SeatLock not found with id: " + id);
        }
        seatLockRepository.deleteById(id);
    }

    // ✅ unlock nhiều ghế
    @Transactional
    public void unlockSeats(Long showtimeId, List<Long> seatIds) {
        seatLockRepository.deleteByShowtimeIdAndSeatIdIn(showtimeId, seatIds);
    }

    // ✅ auto clear sau mỗi 1 phút (quá hạn 10p sẽ bị xóa)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void clearExpiredLocks() {
        seatLockRepository.deleteByLockedUntilBefore(LocalDateTime.now());
    }
}