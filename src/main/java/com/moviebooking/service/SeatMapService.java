package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SeatMapService {

    @Autowired private PricingRuleRepository pricingRuleRepo;
    @Autowired private SeatMapConfigRepository seatMapConfigRepo;

    /**
     * Công thức tính giá:
     *
     *   Giá cuối = showtime.basePrice
     *              × hệ số loại ghế
     *              × (1 - discount% khán giả / 100)
     *
     * Ví dụ showtime.basePrice = 100.000đ:
     *   NORMAL + ADULT   = 100.000 × 1.0 × 1.00 = 100.000đ
     *   NORMAL + CHILD   = 100.000 × 1.0 × 0.50 =  50.000đ
     *   VIP    + ADULT   = 100.000 × 1.3 × 1.00 = 130.000đ
     *   VIP    + STUDENT = 100.000 × 1.3 × 0.80 = 104.000đ
     *   COUPLE + SENIOR  = 100.000 × 1.6 × 0.70 = 112.000đ
     */
    public BigDecimal calculatePrice(Showtime showtime, String seatType,
                                     Long cinemaId, String audienceType) {

        PricingRule.SeatType     st = parseSeatType(seatType);
        PricingRule.AudienceType at = parseAudienceType(audienceType);

        // ── 1. Giá gốc từ suất chiếu (admin nhập khi tạo showtime) ──
        BigDecimal price = showtime.getBasePrice();

        // ── 2. Hệ số nhân theo loại ghế ─────────────────────────────
        //    NORMAL × 1.0  |  VIP × 1.3  |  COUPLE × 1.6
        double multiplier = switch (st) {
            case VIP    -> 1.3;
            case COUPLE -> 1.6;
            default     -> 1.0;
        };
        if (multiplier != 1.0) {
            price = price.multiply(BigDecimal.valueOf(multiplier))
                    .setScale(0, RoundingMode.HALF_UP);
        }

        // ── 3. Giảm giá theo loại khán giả (từ pricing_rules) ───────
        int discountPct = fetchDiscountPercent(cinemaId, st, at);
        if (discountPct > 0) {
            BigDecimal discount = price
                    .multiply(BigDecimal.valueOf(discountPct))
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
            price = price.subtract(discount).setScale(0, RoundingMode.HALF_UP);
        }

        return price;
    }

    /**
     * Lấy discount% từ pricing_rules.
     * Ưu tiên: rule theo rạp cụ thể → rule global (cinema_id IS NULL) → 0
     */
    private int fetchDiscountPercent(Long cinemaId,
                                     PricingRule.SeatType seatType,
                                     PricingRule.AudienceType audienceType) {
        return pricingRuleRepo
                .findByCinemaIdAndSeatTypeAndAudienceType(cinemaId, seatType, audienceType)
                .or(() -> pricingRuleRepo
                        .findByCinemaIsNullAndSeatTypeAndAudienceType(seatType, audienceType))
                .map(r -> r.getDiscountPercent() != null ? r.getDiscountPercent() : 0)
                .orElse(0);
    }

    // ── Các hàm phụ trợ ──────────────────────────────────────────────

    public SeatMapConfig getSeatMapByRoom(Long roomId) {
        return seatMapConfigRepo.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Chưa có cấu hình sơ đồ ghế cho phòng: " + roomId));
    }

    private PricingRule.SeatType parseSeatType(String s) {
        try { return PricingRule.SeatType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return PricingRule.SeatType.NORMAL; }
    }

    private PricingRule.AudienceType parseAudienceType(String s) {
        if (s == null) return PricingRule.AudienceType.ADULT;
        try { return PricingRule.AudienceType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return PricingRule.AudienceType.ADULT; }
    }
}