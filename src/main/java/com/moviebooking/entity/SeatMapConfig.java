package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_map_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeatMapConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Integer totalRows;

    @Column(nullable = false)
    private Integer seatsPerRow;

    // JSON string: {"A":[1,2,3],"B":[1,2]} - danh sách ghế VIP theo hàng
    @Column(columnDefinition = "TEXT")
    private String vipRowsJson;

    // JSON string: {"A":[1,2]} - danh sách ghế bị chặn (không bán)
    @Column(columnDefinition = "TEXT")
    private String blockedSeatsJson;
}