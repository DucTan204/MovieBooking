package com.moviebooking.repository;

import com.moviebooking.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    // Tìm theo rạp + loại ghế + loại khán giả
    Optional<PricingRule> findByCinemaIdAndSeatTypeAndAudienceType(
            Long cinemaId,
            PricingRule.SeatType seatType,
            PricingRule.AudienceType audienceType
    );

    // Fallback: tìm rule mặc định (không gắn rạp cụ thể)
    Optional<PricingRule> findByCinemaIsNullAndSeatTypeAndAudienceType(
            PricingRule.SeatType seatType,
            PricingRule.AudienceType audienceType
    );

    List<PricingRule> findByCinemaId(Long cinemaId);
}