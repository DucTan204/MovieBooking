package com.moviebooking.controller;

import com.moviebooking.entity.TicketQR;
import com.moviebooking.service.TicketQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets/qr")
public class TicketQRController {

    @Autowired private TicketQRService ticketQRService;

    /** User xem QR vé của booking mình */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getQR(@PathVariable Long bookingId) {
        TicketQR qr = ticketQRService.getByBookingId(bookingId);
        return ResponseEntity.ok(Map.of(
                "qrCode",           qr.getQrCode(),
                "qrImageBase64",    qr.getQrImageBase64(),
                "blockchainTxHash", qr.getBlockchainTxHash() != null ? qr.getBlockchainTxHash() : "",
                "status",           qr.getStatus().toString(),
                "createdAt",        qr.getCreatedAt().toString()
        ));
    }

    /**
     * ✅ ADMIN: Generate (nếu chưa có) + lấy QR của bất kỳ booking nào
     * Bypass ownership check, tự tạo QR nếu chưa tồn tại
     */
    @GetMapping("/admin/booking/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getOrGenerateQRForAdmin(@PathVariable Long bookingId) {
        TicketQR qr = ticketQRService.generateQRForBooking(bookingId);
        return ResponseEntity.ok(Map.of(
                "qrCode",           qr.getQrCode(),
                "qrImageBase64",    qr.getQrImageBase64(),
                "blockchainTxHash", qr.getBlockchainTxHash() != null ? qr.getBlockchainTxHash() : "",
                "status",           qr.getStatus().toString(),
                "createdAt",        qr.getCreatedAt().toString()
        ));
    }

    /** Nhân viên quét QR hoặc nhập mã tay */
    @PostMapping("/verify")
    @PreAuthorize("hasAnyAuthority('ADMIN','STAFF')")
    public ResponseEntity<?> verifyTicket(@RequestBody Map<String, String> body) {
        String code = body.get("qrCode");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("Mã vé không được để trống");
        }
        Map<String, Object> result = ticketQRService.verifyTicketAnyCode(code);
        return ResponseEntity.ok(result);
    }
}