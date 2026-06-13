package com.moviebooking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cinema_id", nullable = false)
    // Chỉ lấy thông tin cơ bản của Cinema, tránh lấy danh sách rooms ngược lại
    @JsonIgnoreProperties("rooms")
    private Cinema cinema;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;

    // Chặn lấy danh sách ghế và suất chiếu khi đang ở trong Room để giảm tải dữ liệu
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Seat> seats;

    public enum RoomStatus {
        ACTIVE,
        MAINTENANCE,
        INACTIVE
    }
}