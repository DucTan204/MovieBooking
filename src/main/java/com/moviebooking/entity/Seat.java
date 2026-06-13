package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "seat_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "seat_row", length = 5)
    private String seatRow;

    @Enumerated(EnumType.STRING)
    private SeatType type = SeatType.NORMAL;

    // Thêm COUPLE để khớp SQL ENUM
    public enum SeatType { NORMAL, VIP, COUPLE }
}