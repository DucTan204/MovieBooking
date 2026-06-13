package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerCareService {

    @Autowired private MovieCommentRepository commentRepo;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private UserVoucherRepository userVoucherRepo;
    @Autowired private BookingRepository bookingRepo;

    // ─── COMMENT ──────────────────────────────────────────────────────────

    public MovieComment addComment(Long movieId, Long userId, String content, int rating) {
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");

        Movie movie = new Movie(); movie.setId(movieId);
        User user = new User(); user.setId(userId);

        MovieComment comment = new MovieComment();
        comment.setMovie(movie);
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);
        return commentRepo.save(comment);
    }

    public List<MovieComment> getApprovedComments(Long movieId) {
        return commentRepo.findByMovieIdAndIsApprovedTrueOrderByCreatedAtDesc(movieId);
    }

    public Double getAverageRating(Long movieId) {
        Double avg = commentRepo.findAvgRatingByMovieId(movieId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // ─── VOUCHER ──────────────────────────────────────────────────────────

    @Transactional
    public BigDecimal applyVoucher(Long userId, String code, BigDecimal orderAmount) {
        Voucher voucher = voucherRepo.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Mã voucher không hợp lệ hoặc đã hết hạn"));

        // Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate()))
            throw new RuntimeException("Voucher chưa đến thời gian sử dụng");
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate()))
            throw new RuntimeException("Voucher đã hết hạn");

        // Kiểm tra đơn hàng tối thiểu
        if (orderAmount.compareTo(voucher.getMinOrderAmount()) < 0)
            throw new RuntimeException("Đơn hàng chưa đủ điều kiện áp dụng voucher");

        // Kiểm tra user đã dùng chưa
        if (userVoucherRepo.existsByUserIdAndVoucherId(userId, voucher.getId()))
            throw new RuntimeException("Bạn đã sử dụng voucher này rồi");

        // Kiểm tra giới hạn số lần dùng
        if (voucher.getUsageLimit() != null) {
            long usedCount = userVoucherRepo.countByVoucherId(voucher.getId());
            if (usedCount >= voucher.getUsageLimit())
                throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        // Tính giảm giá
        BigDecimal discount;
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {
            discount = orderAmount
                    .multiply(BigDecimal.valueOf(voucher.getDiscountValue().doubleValue() / 100))
                    .setScale(0, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null)
                discount = discount.min(voucher.getMaxDiscountAmount());
        } else {
            discount = voucher.getDiscountValue();
        }

        return discount.min(orderAmount); // không giảm quá tổng tiền
    }

    // ─── AUDIENCE DISCOUNT ────────────────────────────────────────────────

    /** Giảm giá theo loại khán giả (áp dụng nếu không có PricingRule riêng) */
    public BigDecimal applyAudienceDiscount(BigDecimal basePrice, String audienceType) {
        if (audienceType == null) return basePrice;
        return switch (audienceType.toUpperCase()) {
            case "CHILD"   -> basePrice.multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_UP);  // -50%
            case "STUDENT" -> basePrice.multiply(BigDecimal.valueOf(0.8)).setScale(0, RoundingMode.HALF_UP);  // -20%
            case "SENIOR"  -> basePrice.multiply(BigDecimal.valueOf(0.7)).setScale(0, RoundingMode.HALF_UP);  // -30%
            default        -> basePrice; // ADULT: không giảm
        };
    }
}