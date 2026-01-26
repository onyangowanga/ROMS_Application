package com.roms.entity;

import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
       indexes = {
           @Index(name = "idx_candidate_payment", columnList = "candidate_id"),
           @Index(name = "idx_transaction_ref", columnList = "transaction_ref"),
           @Index(name = "idx_payment_date", columnList = "payment_date")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Column(name = "transaction_ref", unique = true, length = 100)
    private String transactionRef;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "mpesa_ref", length = 100)
    private String mpesaRef;

    @Column(length = 500)
    private String description;

    @Column(name = "is_reversal", nullable = false)
    @Builder.Default
    private Boolean isReversal = false;

    @Column(name = "linked_transaction_id")
    private Long linkedTransactionId;

    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;

    @Column(name = "reversed_by", length = 100)
    private String reversedBy;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (transactionRef == null) {
            transactionRef = "PAY" + System.currentTimeMillis();
        }
    }

    /**
     * Create a reversal payment
     */
    public Payment createReversal(String reason, String reversedBy) {
        return Payment.builder()
                .candidate(this.candidate)
                .amount(this.amount.negate())
                .type(this.type == PaymentType.DEBIT ? PaymentType.CREDIT : PaymentType.DEBIT)
                .paymentMethod(this.paymentMethod)
                .description("REVERSAL: " + this.description)
                .isReversal(true)
                .linkedTransactionId(this.id)
                .reversedAt(LocalDateTime.now())
                .reversedBy(reversedBy)
                .reversalReason(reason)
                .build();
    }
}
