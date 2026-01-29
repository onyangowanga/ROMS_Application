package com.roms.dto;

import com.roms.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateRefNo;
    private Long jobOrderId;
    private String jobOrderRef;
    private String jobTitle;
    private AssignmentStatus status;
    private Boolean isActive;
    private LocalDateTime assignedAt;
    private LocalDateTime offerIssuedAt;
    private LocalDateTime placementConfirmedAt;
    private LocalDateTime cancelledAt;
    private String notes;
}
