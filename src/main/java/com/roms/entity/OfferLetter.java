package com.roms.entity;

import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.OfferLetterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an employment offer letter in the recruitment process
 * Critical business entity - governs the formal offer issuance and acceptance workflow
 */
@Entity
@Table(name = "offer_letters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferLetter extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the candidate receiving the offer
     * Required - every offer belongs to a candidate
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * Reference to the specific job order for this offer
     * Required - ties offer to specific job requisition
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_order_id", nullable = false)
    private JobOrder jobOrder;

    /**
     * Current status of the offer letter
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OfferLetterStatus status;

    /**
     * Timestamp when offer was issued to candidate
     * Null when status is DRAFT
     */
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    /**
     * Timestamp when candidate signed the offer
     * Null until status becomes SIGNED
     */
    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    /**
     * Reference to the offer letter document (PDF/DOCX)
     * Optional - may be generated inline or attached later
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private CandidateDocument document;

    /**
     * Additional notes or conditions attached to this offer
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Salary offered in the letter
     */
    @Column(name = "offered_salary")
    private Double offeredSalary;

    /**
     * Job title as stated in the offer letter
     */
    @Column(name = "job_title", length = 200)
    private String jobTitle;

    /**
     * Start date proposed in the offer
     */
    @Column(name = "proposed_start_date")
    private LocalDateTime proposedStartDate;
}
