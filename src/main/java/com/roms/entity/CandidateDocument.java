package com.roms.entity;

import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Entity
@Table(name = "candidate_documents",
       indexes = {
           @Index(name = "idx_doc_type", columnList = "doc_type"),
           @Index(name = "idx_candidate_doc", columnList = "candidate_id, doc_type")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class CandidateDocument extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private DocumentType docType;

    @NotBlank
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Column(name = "drive_file_id", nullable = false, unique = true, length = 500)
    private String driveFileId;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(length = 500)
    private String description;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    /**
     * Check if document is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if document is valid for at least given months
     */
    public boolean isValidForMonths(int months) {
        if (expiryDate == null) {
            return true; // No expiry
        }
        return expiryDate.isAfter(LocalDate.now().plusMonths(months));
    }
}
