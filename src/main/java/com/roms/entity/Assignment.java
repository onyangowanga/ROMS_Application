package com.roms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments",
        indexes = {
                @Index(name = "idx_assignment_candidate", columnList = "candidate_id"),
                @Index(name = "idx_assignment_job_order", columnList = "job_order_id"),
                @Index(name = "idx_assignment_status", columnList = "status"),
                @Index(name = "idx_assignment_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_active_candidate_assignment",
                        columnNames = {"candidate_id", "is_active"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Assignment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "assignments"})
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "employer", "candidates"})
    private JobOrder jobOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "offer_issued_at")
    private LocalDateTime offerIssuedAt;

    @Column(name = "placement_confirmed_at")
    private LocalDateTime placementConfirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(length = 1000)
    private String notes;

    /**
     * Mark this assignment as inactive
     */
    public void deactivate() {
        this.isActive = false;
        if (this.status != AssignmentStatus.CANCELLED) {
            this.status = AssignmentStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
        }
    }

    /**
     * Issue offer for this assignment
     */
    public void issueOffer() {
        if (this.offerIssuedAt == null) {
            this.offerIssuedAt = LocalDateTime.now();
            this.status = AssignmentStatus.OFFERED;
        }
    }

    /**
     * Confirm placement for this assignment
     */
    public void confirmPlacement() {
        if (this.placementConfirmedAt == null) {
            this.placementConfirmedAt = LocalDateTime.now();
            this.status = AssignmentStatus.PLACED;
        }
    }
}
