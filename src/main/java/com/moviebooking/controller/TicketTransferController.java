package com.moviebooking.controller;

import com.moviebooking.dto.TicketTransferDTO;
import com.moviebooking.entity.User;
import com.moviebooking.service.AuthService;
import com.moviebooking.service.TicketTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket-transfers")
public class TicketTransferController {

    @Autowired private TicketTransferService transferService;
    @Autowired private AuthService authService;

    // ── USER APIs ─────────────────────────────────────────────────────

    /** User: Gửi yêu cầu hoàn vé kèm thông tin ngân hàng */
    @PostMapping
    public ResponseEntity<TicketTransferDTO> requestRefund(
            @RequestBody Map<String, Object> body) {

        User currentUser = authService.getCurrentUser();
        Long bookingId           = Long.valueOf(body.get("bookingId").toString());
        String reason            = (String) body.getOrDefault("reason", "");
        String bankName          = (String) body.getOrDefault("bankName", "");
        String bankAccountNumber = (String) body.getOrDefault("bankAccountNumber", "");
        String bankAccountName   = (String) body.getOrDefault("bankAccountName", "");

        TicketTransferDTO result = transferService.requestRefund(
                bookingId, currentUser.getId(),
                reason, bankName, bankAccountNumber, bankAccountName
        );
        return ResponseEntity.ok(result);
    }

    /** User: Xem lịch sử hoàn vé của chính mình */
    @GetMapping("/my")
    public ResponseEntity<List<TicketTransferDTO>> getMyTransfers() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(transferService.getMyTransfers(currentUser.getId()));
    }

    /** User: Xem chi tiết 1 yêu cầu hoàn vé */
    @GetMapping("/{id}")
    public ResponseEntity<TicketTransferDTO> getById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(transferService.getByIdForUser(id, currentUser.getId()));
    }

    // ── ADMIN APIs ────────────────────────────────────────────────────

    /** Admin: Lấy tất cả yêu cầu hoàn vé */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TicketTransferDTO>> getAll(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(transferService.getAll(status));
    }

    /**
     * ✅ ADMIN: Tạo yêu cầu hoàn vé thay cho user (bypass ownership check)
     * Dùng khi admin thao tác trực tiếp từ trang quản lý đặt vé
     */
    @PostMapping("/admin/request")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TicketTransferDTO> adminRequestRefund(
            @RequestBody Map<String, Object> body) {

        Long bookingId           = Long.valueOf(body.get("bookingId").toString());
        Long userId              = Long.valueOf(body.get("userId").toString());
        String reason            = (String) body.getOrDefault("reason", "Admin tạo yêu cầu hoàn vé");
        String bankName          = (String) body.getOrDefault("bankName", "");
        String bankAccountNumber = (String) body.getOrDefault("bankAccountNumber", "");
        String bankAccountName   = (String) body.getOrDefault("bankAccountName", "");

        TicketTransferDTO result = transferService.requestRefund(
                bookingId, userId,
                reason, bankName, bankAccountNumber, bankAccountName
        );
        return ResponseEntity.ok(result);
    }

    /** Admin: Duyệt hoàn vé */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TicketTransferDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.approveTransfer(id));
    }

    /** Admin: Từ chối hoàn vé kèm lý do */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TicketTransferDTO> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String rejectReason = body.getOrDefault("reason", "Không đủ điều kiện hoàn vé");
        return ResponseEntity.ok(transferService.rejectTransfer(id, rejectReason));
    }
}