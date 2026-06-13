package com.moviebooking.service;

import com.moviebooking.dto.BookingDTO;
import com.moviebooking.dto.ShowtimeDTO;
import com.moviebooking.dto.TicketDTO;
import com.moviebooking.entity.*;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final SeatLockRepository seatLockRepository;

    @Autowired private SeatMapService seatMapService;
    @Autowired private CustomerCareService customerCareService;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private UserVoucherRepository userVoucherRepo;

    /**
     * Chuyển đổi từ Booking Entity sang BookingDTO
     */
    private BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingCode(booking.getBookingCode());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().toString() : "UNKNOWN");
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setVoucherDiscount(booking.getVoucherDiscount());

        if (booking.getAppliedVoucher() != null) {
            dto.setAppliedVoucherCode(booking.getAppliedVoucher().getCode());
        }
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
        }

        Showtime st = booking.getShowtime();
        if (st != null) {
            ShowtimeDTO stDto = new ShowtimeDTO();
            stDto.setId(st.getId());
            stDto.setMovieTitle(st.getMovie() != null ? st.getMovie().getTitle() : "N/A");
            stDto.setRoomName(st.getRoom() != null ? st.getRoom().getName() : "N/A");

            // ✅ FIX 1: Map Cinema Name vào ShowtimeDTO
            if (st.getRoom() != null && st.getRoom().getCinema() != null) {
                stDto.setCinemaName(st.getRoom().getCinema().getName());
            } else {
                stDto.setCinemaName("N/A");
            }

            stDto.setStartTime(st.getStartTime());
            dto.setShowtime(stDto);
            dto.setShowtimeId(st.getId());
        }

        try {
            if (booking.getTickets() != null) {
                List<TicketDTO> ticketDTOs = booking.getTickets().stream().map(t -> {
                    TicketDTO td = new TicketDTO();
                    td.setId(t.getId());
                    td.setSeatNumber(t.getSeat() != null ? t.getSeat().getSeatNumber() : "??");
                    td.setPrice(t.getPrice());

                    // ✅ FIX 2: Map AudienceType từ Ticket Entity sang TicketDTO
                    td.setAudienceType(t.getAudienceType());

                    return td;
                }).collect(Collectors.toList());
                dto.setTickets(ticketDTOs);
            }
        } catch (Exception e) {
            dto.setTickets(new ArrayList<>());
        }
        return dto;
    }

    @Transactional
    public BookingDTO create(BookingDTO dto) {
        // 1. Lấy thông tin User từ Security Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // 2. Lấy thông tin Suất chiếu
        Showtime showtime = showtimeRepository.findById(dto.getShowtimeId())
                .orElseThrow(() -> new NotFoundException("Suất chiếu không tồn tại"));

        BigDecimal total = BigDecimal.ZERO;
        Long cinemaId = showtime.getRoom().getCinema().getId();
        List<Ticket> ticketsToSave = new ArrayList<>();

        // 3. Xử lý từng ghế được chọn
        for (int i = 0; i < dto.getSeatIds().size(); i++) {
            Long seatId = dto.getSeatIds().get(i);
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new NotFoundException("Ghế ID " + seatId + " không tồn tại"));

            // Kiểm tra xem ghế đã được đặt chính thức chưa
            boolean isBooked = ticketRepository.existsByBooking_Showtime_IdAndSeat_Id(showtime.getId(), seatId);
            if (isBooked) {
                throw new RuntimeException("Ghế " + seat.getSeatNumber() + " đã có người đặt!");
            }

            // Xác định loại đối tượng (Người lớn, Trẻ em, Sinh viên...)
            String audience = "ADULT";
            if (dto.getAudienceTypes() != null && i < dto.getAudienceTypes().size()) {
                audience = dto.getAudienceTypes().get(i);
            } else if (dto.getAudienceType() != null && !dto.getAudienceType().isBlank()) {
                audience = dto.getAudienceType();
            }

            // Tính giá vé dựa trên loại ghế và loại đối tượng
            BigDecimal seatPrice = seatMapService.calculatePrice(
                    showtime, seat.getType().toString(), cinemaId, audience
            );
            total = total.add(seatPrice);

            // Tạo Entity Ticket
            Ticket ticket = new Ticket();
            ticket.setSeat(seat);
            ticket.setPrice(seatPrice);

            // ✅ FIX 3: Lưu loại đối tượng (AudienceType) vào Ticket Entity
            ticket.setAudienceType(audience);

            ticketsToSave.add(ticket);
        }

        // 4. Xử lý Voucher (nếu có)
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        Voucher appliedVoucher = null;

        String inputCode = (dto.getVoucherCode() != null && !dto.getVoucherCode().isBlank())
                ? dto.getVoucherCode().trim()
                : (dto.getAppliedVoucherCode() != null && !dto.getAppliedVoucherCode().isBlank()
                ? dto.getAppliedVoucherCode().trim()
                : null);

        if (inputCode != null) {
            try {
                voucherDiscount = customerCareService.applyVoucher(user.getId(), inputCode, total);
                total = total.subtract(voucherDiscount);
                if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

                appliedVoucher = voucherRepo.findByCodeAndIsActiveTrue(inputCode).orElse(null);
            } catch (Exception e) {
                // Nếu áp dụng voucher lỗi thì bỏ qua discount
                voucherDiscount = BigDecimal.ZERO;
            }
        }

        // 5. Tạo và lưu Booking
        Booking booking = new Booking();
        booking.setBookingCode("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setTotalPrice(total);
        booking.setVoucherDiscount(voucherDiscount);
        booking.setAppliedVoucher(appliedVoucher);
        booking.setStatus(Booking.BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        // 6. Liên kết Ticket với Booking và lưu Ticket
        for (Ticket ticket : ticketsToSave) {
            ticket.setBooking(savedBooking);
            ticketRepository.save(ticket);
        }

        // 7. Lưu lịch sử sử dụng Voucher của User
        if (appliedVoucher != null) {
            UserVoucher uv = new UserVoucher();
            uv.setUser(user);
            uv.setVoucher(appliedVoucher);
            uv.setBooking(savedBooking);
            userVoucherRepo.save(uv);
        }

        // 8. Giải phóng SeatLock (nếu có)
        seatLockRepository.deleteByShowtimeIdAndSeatIdIn(showtime.getId(), dto.getSeatIds());

        return toDTO(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> findMyBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingDTO findById(Long id) {
        return toDTO(bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng")));
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> findAll() {
        return bookingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
}