package com.roms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.CommissionAgreementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_agreements",
        indexes = {
                @Index(name = "idx_commission_candidate", columnList = "candidate_id"),
                @Index(name = "idx_commission_assignment", columnList = "assignment_id"),
                @Index(name = "idx_commission_status", columnList = "status"),
                @Index(name = "idx_commission_active", columnList = "is_active")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class CommissionAgreement extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Assignment assignment;

    @Column(name = "agreement_number", nullable = false, unique = true, length = 50)
    private String agreementNumber;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "base_salary", precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CommissionAgreementStatus status = CommissionAgreementStatus.DRAFT;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(length = 2000)
    private String terms;

    @Column(length = 1000)
    private String notes;

    /**
     * Mark agreement as signed
     */
    public void sign() {
        if (this.status == CommissionAgreementStatus.PENDING_SIGNATURE) {
            this.signedAt = LocalDateTime.now();
            this.status = CommissionAgreementStatus.SIGNED;
        }
    }

    /**
     * Activate the agreement
     */
    public void activate() {
        if (this.status == CommissionAgreementStatus.SIGNED) {
            this.status = CommissionAgreementStatus.ACTIVE;
        }
    }

    /**
     * Cancel the agreement
     */
    public void cancel(String reason) {
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.status = CommissionAgreementStatus.CANCELLED;
        this.setIsActive(false);
    }
}
