// com/moviebooking/controller/SeatMapController.java
package com.moviebooking.controller;

import com.moviebooking.dto.PricingRuleDTO;
import com.moviebooking.entity.Cinema;
import com.moviebooking.entity.PricingRule;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.CinemaRepository;
import com.moviebooking.repository.PricingRuleRepository;
import com.moviebooking.service.SeatMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seat-map")
public class SeatMapController {

    @Autowired private SeatMapService seatMapService;
    @Autowired private PricingRuleRepository pricingRuleRepo;
    @Autowired private CinemaRepository cinemaRepo;

    // ── Sơ đồ ghế ────────────────────────────────────────────────────

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getSeatMapByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(seatMapService.getSeatMapByRoom(roomId));
    }

    // ── Bảng giá – READ ───────────────────────────────────────────────

    /** Public: Lấy bảng giá theo rạp (dùng ở trang đặt vé) */
    @GetMapping("/pricing/{cinemaId}")
    public ResponseEntity<List<PricingRuleDTO>> getPricingByCinema(@PathVariable Long cinemaId) {
        List<PricingRuleDTO> list = pricingRuleRepo.findByCinemaId(cinemaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Admin: Lấy toàn bộ pricing rules (để quản lý) */
    @GetMapping("/pricing")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PricingRuleDTO>> getAllPricing() {
        List<PricingRuleDTO> list = pricingRuleRepo.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Admin: Lấy 1 rule theo ID */
    @GetMapping("/pricing/detail/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PricingRuleDTO> getPricingById(@PathVariable Long id) {
        PricingRule rule = pricingRuleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy pricing rule: " + id));
        return ResponseEntity.ok(toDTO(rule));
    }

    // ── Bảng giá – WRITE ──────────────────────────────────────────────

    /** Admin: Tạo rule giá mới */
    @PostMapping("/pricing")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PricingRuleDTO> createPricingRule(@RequestBody PricingRuleDTO dto) {
        PricingRule rule = fromDTO(dto);
        return ResponseEntity.ok(toDTO(pricingRuleRepo.save(rule)));
    }

    /** Admin: Cập nhật rule giá */
    @PutMapping("/pricing/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PricingRuleDTO> updatePricingRule(
            @PathVariable Long id,
            @RequestBody PricingRuleDTO dto) {

        PricingRule rule = pricingRuleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy pricing rule: " + id));

        // Cập nhật cinema (có thể null = global)
        if (dto.getCinemaId() != null) {
            Cinema cinema = cinemaRepo.findById(dto.getCinemaId())
                    .orElseThrow(() -> new NotFoundException("Rạp không tồn tại"));
            rule.setCinema(cinema);
        } else {
            rule.setCinema(null); // global rule
        }

        if (dto.getSeatType() != null)
            rule.setSeatType(PricingRule.SeatType.valueOf(dto.getSeatType().toUpperCase()));
        if (dto.getAudienceType() != null)
            rule.setAudienceType(PricingRule.AudienceType.valueOf(dto.getAudienceType().toUpperCase()));
        if (dto.getBasePrice() != null)
            rule.setBasePrice(dto.getBasePrice());
        if (dto.getDiscountPercent() != null)
            rule.setDiscountPercent(dto.getDiscountPercent());

        return ResponseEntity.ok(toDTO(pricingRuleRepo.save(rule)));
    }

    /** Admin: Xoá rule giá */
    @DeleteMapping("/pricing/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePricingRule(@PathVariable Long id) {
        if (!pricingRuleRepo.existsById(id))
            throw new NotFoundException("Không tìm thấy pricing rule: " + id);
        pricingRuleRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xoá pricing rule"));
    }

    // ── Helper ────────────────────────────────────────────────────────

    private PricingRuleDTO toDTO(PricingRule r) {
        return PricingRuleDTO.builder()
                .id(r.getId())
                .cinemaId(r.getCinema() != null ? r.getCinema().getId() : null)
                .cinemaName(r.getCinema() != null ? r.getCinema().getName() : "Toàn hệ thống")
                .seatType(r.getSeatType() != null ? r.getSeatType().name() : null)
                .audienceType(r.getAudienceType() != null ? r.getAudienceType().name() : null)
                .basePrice(r.getBasePrice())
                .discountPercent(r.getDiscountPercent())
                .build();
    }

    private PricingRule fromDTO(PricingRuleDTO dto) {
        PricingRule rule = new PricingRule();
        if (dto.getCinemaId() != null) {
            Cinema cinema = cinemaRepo.findById(dto.getCinemaId())
                    .orElseThrow(() -> new NotFoundException("Rạp không tồn tại"));
            rule.setCinema(cinema);
        }
        if (dto.getSeatType() != null)
            rule.setSeatType(PricingRule.SeatType.valueOf(dto.getSeatType().toUpperCase()));
        if (dto.getAudienceType() != null)
            rule.setAudienceType(PricingRule.AudienceType.valueOf(dto.getAudienceType().toUpperCase()));
        rule.setBasePrice(dto.getBasePrice());
        rule.setDiscountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0);
        return rule;
    }
}