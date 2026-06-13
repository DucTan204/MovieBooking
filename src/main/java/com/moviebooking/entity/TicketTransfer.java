package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_transfers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TicketTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne @JoinColumn(name = "from_user_id")
    private User fromUser;

    @Column(name = "original_amount", nullable = false)
    private BigDecimal originalAmount;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private TransferStatus status = TransferStatus.PENDING;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", length = 200)
    private String bankAccountName;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    public enum TransferStatus { PENDING, APPROVED, REJECTED, EXPIRED }
}