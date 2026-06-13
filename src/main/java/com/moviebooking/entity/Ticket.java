package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"booking_id", "seat_id"})})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    // ✅ THÊM DÒNG NÀY: Để lưu loại đối tượng (ADULT, CHILD, STUDENT...)
    @Column(name = "audience_type")
    private String audienceType;
}