package com.roms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotNull(message = "Job Order ID is required")
    private Long jobOrderId;

    private String notes;
}
