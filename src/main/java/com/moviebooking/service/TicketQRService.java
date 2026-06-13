package com.moviebooking.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.moviebooking.entity.*;
import com.moviebooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketQRService {

    @Autowired private TicketQRRepository ticketQRRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private BlockchainService blockchainService;

    @Value("${app.qr.secret-salt:DefaultSalt@2025}")
    private String secretSalt;

    @Transactional
    public TicketQR generateQRForBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        return ticketQRRepo.findByBookingId(bookingId).orElseGet(() -> {
            try {
                String rawData = bookingId + "_" + booking.getUser().getId()
                        + "_" + booking.getCreatedAt() + "_" + secretSalt;
                String qrCode = sha256(rawData).substring(0, 32).toUpperCase();
                String qrImageBase64 = generateQRImage(qrCode, 300, 300);
                String txHash = blockchainService.registerTicketHash(qrCode, bookingId);

                TicketQR ticketQR = new TicketQR();
                ticketQR.setBooking(booking);
                ticketQR.setQrCode(qrCode);
                ticketQR.setQrImageBase64(qrImageBase64);
                ticketQR.setBlockchainTxHash(txHash);
                ticketQR.setStatus(TicketQR.QRStatus.VALID);
                return ticketQRRepo.save(ticketQR);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate QR: " + e.getMessage(), e);
            }
        });
    }

    @Transactional
    public Map<String, Object> verifyTicketAnyCode(String inputCode) {
        Booking booking;
        TicketQR qrRecord = null;

        // 1. Thử tìm theo QR Code (Quét từ Camera)
        Optional<TicketQR> qrOpt = ticketQRRepo.findByQrCode(inputCode);

        if (qrOpt.isPresent()) {
            qrRecord = qrOpt.get();
            booking = qrRecord.getBooking();
        } else {
            // 2. Nếu không thấy, thử tìm theo Mã đặt vé (Nhập tay)
            booking = bookingRepo.findByBookingCode(inputCode)
                    .orElseThrow(() -> new RuntimeException("Mã vé hoặc QR không hợp lệ!"));
            qrRecord = ticketQRRepo.findByBookingId(booking.getId()).orElse(null);
        }

        // 3. Kiểm tra trạng thái đơn hàng
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw new RuntimeException("Vé này đã bị hủy hoặc hoàn tiền!");

        if (qrRecord != null && qrRecord.getStatus() == TicketQR.QRStatus.USED)
            throw new RuntimeException("Vé này đã sử dụng vào lúc: " + qrRecord.getVerifiedAt());

        // 4. Cập nhật trạng thái vé thành ĐÃ DÙNG
        LocalDateTime verifiedAt = LocalDateTime.now();
        if (qrRecord != null) {
            qrRecord.setStatus(TicketQR.QRStatus.USED);
            qrRecord.setVerifiedAt(verifiedAt);
            ticketQRRepo.save(qrRecord);
        }

        // 5. Trả về thông tin đầy đủ cho quản trị viên
        // Dùng HashMap thay Map.of() để tránh giới hạn 10 entries
        Map<String, Object> result = new HashMap<>();
        result.put("bookingCode",  booking.getBookingCode());
        result.put("customerName", booking.getUser().getName());
        result.put("movieTitle",   booking.getShowtime().getMovie().getTitle());
        result.put("cinemaName",   booking.getShowtime().getRoom().getCinema().getName());
        result.put("roomName",     booking.getShowtime().getRoom().getName());
        result.put("startTime",    booking.getShowtime().getStartTime().toString());
        result.put("seatCount",    booking.getTickets().size());
        result.put("seats",        booking.getTickets().stream()
                .map(t -> t.getSeat().getSeatNumber())
                .collect(Collectors.toList()));
        result.put("verifiedAt",   verifiedAt.toString());
        return result;
    }

    /** Lấy QR theo bookingId */
    public TicketQR getByBookingId(Long bookingId) {
        return ticketQRRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("QR chưa được tạo cho booking này"));
    }

    private String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private String generateQRImage(String content, int width, int height) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}