package com.roms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommissionAgreementRequest {
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    private Long assignmentId;

    @NotNull(message = "Commission rate is required")
    private BigDecimal commissionRate;

    private BigDecimal baseSalary;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String terms;

    private String notes;
}
