package com.roms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.AgreementStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agency Commission Agreement Entity
 * 
 * Represents the financial obligation between applicant and agency.
 * This is NOT a payment - it's the contract/agreement that defines payment terms.
 * 
 * Business Rules:
 * - One active agreement per Assignment
 * - Amounts are immutable once signed
 * - Agreement must exist before commission payments accepted
 * - Downpayment must be paid before visa processing
 * - Full commission must be paid before placement
 */
@Entity
@Table(name = "agency_commission_agreements",
        indexes = {
                @Index(name = "idx_agreement_candidate", columnList = "candidate_id"),
                @Index(name = "idx_agreement_assignment", columnList = "assignment_id"),
                @Index(name = "idx_agreement_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_one_agreement_per_assignment", 
                        columnNames = {"assignment_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class AgencyCommissionAgreement extends BaseAuditEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "assignments", "payments", "documents"})
    @NotNull
    private Candidate candidate;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "candidate", "jobOrder"})
    @NotNull
    private Assignment assignment;

    /**
     * Total agency commission amount (typically ~200,000 KES)
     * IMMUTABLE once agreement is signed
     */
    @NotNull
    @Column(name = "total_commission_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCommissionAmount;

    /**
     * Required downpayment before visa processing (typically 50,000 KES)
     * IMMUTABLE once agreement is signed
     */
    @NotNull
    @Column(name = "required_downpayment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requiredDownpaymentAmount;

    /**
     * Currency code (always KES for Phase 2B)
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "KES";

    /**
     * Date when agreement was created/proposed
     */
    @Column(name = "agreement_date", nullable = false)
    @Builder.Default
    private LocalDateTime agreementDate = LocalDateTime.now();

    /**
     * Whether applicant has signed the agreement
     * Agreement amounts become IMMUTABLE once signed = true
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean signed = false;

    /**
     * Date when applicant signed the agreement
     */
    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    /**
     * URL/path to signed agreement document (PDF)
     * Uploaded by applicant or staff
     */
    @Column(name = "signed_agreement_document_url", length = 500)
    private String signedAgreementDocumentUrl;

    /**
     * Agreement status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AgreementStatus status = AgreementStatus.ACTIVE;

    /**
     * Optional notes about the agreement
     */
    @Column(length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (agreementDate == null) {
            agreementDate = LocalDateTime.now();
        }
        if (currency == null) {
            currency = "KES";
        }
    }

    /**
     * Sign the agreement
     * Once signed, amounts become immutable
     */
    public void sign() {
        if (!this.signed) {
            this.signed = true;
            this.signedAt = LocalDateTime.now();
        }
    }

    /**
     * Complete the agreement (all payments made)
     */
    public void complete() {
        this.status = AgreementStatus.COMPLETED;
    }

    /**
     * Cancel the agreement (SUPER_ADMIN only)
     */
    public void cancel(String reason) {
        this.status = AgreementStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                     "CANCELLED: " + reason + " at " + LocalDateTime.now();
    }

    /**
     * Check if amounts can be modified
     */
    public boolean canModifyAmounts() {
        return !this.signed;
    }
}
