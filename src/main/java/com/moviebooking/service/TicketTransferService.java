// com/moviebooking/service/TicketTransferService.java
package com.moviebooking.service;

import com.moviebooking.dto.TicketTransferDTO;
import com.moviebooking.entity.*;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketTransferService {

    @Autowired private TicketTransferRepository transferRepo;
    @Autowired private BookingRepository         bookingRepo;
    @Autowired private TicketQRRepository        ticketQRRepo;

    private static final double FEE_PERCENT = 0.10; // 10% phí hoàn

    // ── User: Gửi yêu cầu hoàn vé ─────────────────────────────────────

    @Transactional
    public TicketTransferDTO requestRefund(Long bookingId, Long userId,
                                           String reason,
                                           String bankName,
                                           String bankAccountNumber,
                                           String bankAccountName) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy booking: " + bookingId));

        // 1. Kiểm tra chủ sở hữu
        if (!booking.getUser().getId().equals(userId))
            throw new RuntimeException("Bạn không có quyền hoàn vé này");

        // 2. Kiểm tra trạng thái phải là PAID
        if (booking.getStatus() != Booking.BookingStatus.PAID)
            throw new RuntimeException("Vé chưa thanh toán, không thể hoàn");

        // 3. Kiểm tra thời gian: phải trước 24h so với giờ chiếu
        LocalDateTime showtimeStart = booking.getShowtime().getStartTime();
        if (LocalDateTime.now().isAfter(showtimeStart.minusHours(24)))
            throw new RuntimeException("Đã quá thời hạn hoàn vé (phải hoàn trước 24h chiếu)");

        // 4. Kiểm tra chưa có yêu cầu hoàn cho booking này
        if (transferRepo.findByBookingId(bookingId).isPresent())
            throw new RuntimeException("Đã tồn tại yêu cầu hoàn vé cho booking này");

        // 5. Tính tiền hoàn (90%) và phí (10%)
        BigDecimal total  = booking.getTotalPrice();
        BigDecimal fee    = total.multiply(BigDecimal.valueOf(FEE_PERCENT))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal refund = total.subtract(fee);

        // 6. Tạo và lưu TicketTransfer
        TicketTransfer transfer = new TicketTransfer();
        transfer.setBooking(booking);
        transfer.setFromUser(booking.getUser());
        transfer.setOriginalAmount(total);
        transfer.setFeeAmount(fee);
        transfer.setRefundAmount(refund);
        transfer.setDeadline(showtimeStart.minusHours(24)); // ← thêm
        transfer.setStatus(TicketTransfer.TransferStatus.PENDING);
        transfer.setReason(reason);
        transfer.setBankName(bankName);
        transfer.setBankAccountNumber(bankAccountNumber);
        transfer.setBankAccountName(bankAccountName);

        return toDTO(transferRepo.save(transfer));
    }

    // ── Admin: Duyệt hoàn vé ──────────────────────────────────────────

    @Transactional
    public TicketTransferDTO approveTransfer(Long transferId) {
        TicketTransfer transfer = transferRepo.findById(transferId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hoàn vé: " + transferId));

        if (transfer.getStatus() != TicketTransfer.TransferStatus.PENDING)
            throw new RuntimeException("Yêu cầu này đã được xử lý rồi (status: " + transfer.getStatus() + ")");

        // Huỷ QR vé
        ticketQRRepo.findByBookingId(transfer.getBooking().getId()).ifPresent(qr -> {
            qr.setStatus(TicketQR.QRStatus.CANCELLED);
            ticketQRRepo.save(qr);
        });

        // Chuyển booking thành CANCELLED
        Booking booking = transfer.getBooking();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        transfer.setStatus(TicketTransfer.TransferStatus.APPROVED);
        transfer.setProcessedAt(LocalDateTime.now());
        return toDTO(transferRepo.save(transfer));
    }

    // ── Admin: Từ chối hoàn vé ────────────────────────────────────────

    @Transactional
    public TicketTransferDTO rejectTransfer(Long transferId, String rejectReason) {
        TicketTransfer transfer = transferRepo.findById(transferId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu hoàn vé: " + transferId));

        if (transfer.getStatus() != TicketTransfer.TransferStatus.PENDING)
            throw new RuntimeException("Yêu cầu này đã được xử lý rồi (status: " + transfer.getStatus() + ")");

        transfer.setStatus(TicketTransfer.TransferStatus.REJECTED);
        transfer.setRejectReason(rejectReason);
        transfer.setProcessedAt(LocalDateTime.now());
        return toDTO(transferRepo.save(transfer));
    }

    // ── READ ──────────────────────────────────────────────────────────

    public List<TicketTransferDTO> getMyTransfers(Long userId) {
        return transferRepo.findByFromUserIdOrderByRequestedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TicketTransferDTO getByIdForUser(Long transferId, Long userId) {
        TicketTransfer t = transferRepo.findById(transferId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy yêu cầu: " + transferId));
        if (!t.getFromUser().getId().equals(userId))
            throw new RuntimeException("Bạn không có quyền xem yêu cầu này");
        return toDTO(t);
    }

    /** Admin: Lấy tất cả, có thể lọc theo status */
    public List<TicketTransferDTO> getAll(String statusStr) {
        List<TicketTransfer> list;
        if (statusStr != null && !statusStr.isBlank()) {
            TicketTransfer.TransferStatus status =
                    TicketTransfer.TransferStatus.valueOf(statusStr.toUpperCase());
            list = transferRepo.findByStatus(status);
        } else {
            list = transferRepo.findAll();
        }
        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Helper: Entity → DTO ──────────────────────────────────────────

    private TicketTransferDTO toDTO(TicketTransfer t) {
        TicketTransferDTO dto = new TicketTransferDTO();
        dto.setId(t.getId());
        dto.setStatus(t.getStatus().name());
        dto.setRefundAmount(t.getRefundAmount());
        dto.setFeeAmount(t.getFeeAmount());
        dto.setReason(t.getReason());
        dto.setBankName(t.getBankName());
        dto.setBankAccountNumber(t.getBankAccountNumber());
        dto.setBankAccountName(t.getBankAccountName());
        dto.setRejectReason(t.getRejectReason());
        dto.setRequestedAt(t.getRequestedAt());
        dto.setProcessedAt(t.getProcessedAt());

        // Booking info
        if (t.getBooking() != null) {
            Booking b = t.getBooking();
            dto.setBookingId(b.getId());
            dto.setBookingCode(b.getBookingCode());
            dto.setTotalPrice(b.getTotalPrice());

            if (b.getShowtime() != null) {
                dto.setShowtimeStart(b.getShowtime().getStartTime());

                if (b.getShowtime().getMovie() != null)
                    dto.setMovieTitle(b.getShowtime().getMovie().getTitle());

                if (b.getShowtime().getRoom() != null) {
                    dto.setRoomName(b.getShowtime().getRoom().getName());
                    if (b.getShowtime().getRoom().getCinema() != null)
                        dto.setCinemaName(b.getShowtime().getRoom().getCinema().getName());
                }
            }
        }

        // User info
        if (t.getFromUser() != null) {
            dto.setFromUserId(t.getFromUser().getId());
            dto.setFromUserName(t.getFromUser().getName());
            dto.setFromUserEmail(t.getFromUser().getEmail());
        }

        return dto;
    }
}