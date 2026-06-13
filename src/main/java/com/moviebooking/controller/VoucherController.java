// com/moviebooking/controller/VoucherController.java
package com.moviebooking.controller;

import com.moviebooking.entity.Voucher;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.VoucherRepository;
import com.moviebooking.service.CustomerCareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    @Autowired private VoucherRepository voucherRepo;
    @Autowired private CustomerCareService customerCareService;

    // ── READ ─────────────────────────────────────────────────────────

    /** Admin: Lấy tất cả voucher */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Voucher>> getAll() {
        return ResponseEntity.ok(voucherRepo.findAll());
    }

    /** Admin: Lấy chi tiết 1 voucher */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Voucher> getById(@PathVariable Long id) {
        Voucher v = voucherRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher: " + id));
        return ResponseEntity.ok(v);
    }

    /** Public/Authenticated: Lấy danh sách voucher còn hiệu lực (để user xem) */
    @GetMapping("/active")
    public ResponseEntity<List<Voucher>> getActiveVouchers() {
        return ResponseEntity.ok(
                voucherRepo.findByIsActiveTrueAndEndDateAfter(LocalDateTime.now())
        );
    }

    // ── WRITE ─────────────────────────────────────────────────────────

    /** Admin: Tạo voucher mới */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Voucher> create(@RequestBody Voucher voucher) {
        // Đảm bảo trạng thái mặc định
        if (voucher.getIsActive() == null) voucher.setIsActive(true);
        return ResponseEntity.ok(voucherRepo.save(voucher));
    }

    /** Admin: Cập nhật voucher */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Voucher> update(@PathVariable Long id, @RequestBody Voucher updated) {
        Voucher existing = voucherRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher: " + id));

        existing.setCode(updated.getCode());
        existing.setDescription(updated.getDescription());
        existing.setDiscountType(updated.getDiscountType());
        existing.setDiscountValue(updated.getDiscountValue());
        existing.setMaxDiscountAmount(updated.getMaxDiscountAmount());
        existing.setMinOrderAmount(updated.getMinOrderAmount());
        existing.setUsageLimit(updated.getUsageLimit());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setIsActive(updated.getIsActive());

        return ResponseEntity.ok(voucherRepo.save(existing));
    }

    /** Admin: Bật/tắt trạng thái voucher */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long id) {
        Voucher v = voucherRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher: " + id));
        v.setIsActive(!Boolean.TRUE.equals(v.getIsActive()));
        voucherRepo.save(v);
        return ResponseEntity.ok(Map.of(
                "id",       v.getId(),
                "isActive", v.getIsActive(),
                "message",  v.getIsActive() ? "Đã kích hoạt voucher" : "Đã tắt voucher"
        ));
    }

    /** Admin: Xoá voucher */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!voucherRepo.existsById(id))
            throw new NotFoundException("Không tìm thấy voucher: " + id);
        voucherRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xoá voucher"));
    }

    // ── USER API ─────────────────────────────────────────────────────

    /**
     * User: Kiểm tra và áp dụng voucher trước khi đặt vé.
     * Trả về discountAmount và finalAmount để FE hiển thị preview.
     * Lưu ý: đây chỉ là PREVIEW – lưu thực sự xảy ra trong BookingService.create().
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyVoucher(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> body) {

        String code        = (String) body.get("code");
        BigDecimal amount  = new BigDecimal(body.get("orderAmount").toString());
        BigDecimal discount = customerCareService.applyVoucher(userId, code, amount);

        return ResponseEntity.ok(Map.of(
                "code",           code,
                "discountAmount", discount,
                "finalAmount",    amount.subtract(discount)
        ));
    }
}