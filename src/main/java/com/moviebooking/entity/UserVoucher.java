package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "used_at")
    private LocalDateTime usedAt = LocalDateTime.now();

    @ManyToOne @JoinColumn(name = "booking_id")
    private Booking booking;
}