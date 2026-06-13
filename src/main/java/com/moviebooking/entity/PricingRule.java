package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "cinema_id")
    private Cinema cinema; // null = áp dụng toàn hệ thống

    @Enumerated(EnumType.STRING)
    private SeatType seatType; // NORMAL, VIP, COUPLE

    @Enumerated(EnumType.STRING)
    private AudienceType audienceType; // ADULT, CHILD, STUDENT, SENIOR

    @Column(nullable = false)
    private BigDecimal basePrice;

    // Phần trăm giảm giá (0-100)
    private Integer discountPercent = 0;

    public enum SeatType { NORMAL, VIP, COUPLE }
    public enum AudienceType { ADULT, CHILD, STUDENT, SENIOR }
}