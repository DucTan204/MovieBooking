package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_qrs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TicketQR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @Column(unique = true, nullable = false)
    private String qrCode; // mã duy nhất (hash)

    @Column(columnDefinition = "TEXT")
    private String qrImageBase64; // ảnh QR dạng base64

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash; // hash giao dịch blockchain (nếu bật)

    @Enumerated(EnumType.STRING)
    private QRStatus status = QRStatus.VALID;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    public enum QRStatus { VALID, USED, EXPIRED, CANCELLED }
}