package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_locks", uniqueConstraints = {@UniqueConstraint(columnNames = {"showtime_id", "seat_id"})})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeatLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "locked_until", nullable = false)
    private LocalDateTime lockedUntil;
}