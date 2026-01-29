package com.roms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @DecimalMin(value = "0.00", message = "Commission rate must be at least 0")
    @DecimalMax(value = "100.00", message = "Commission rate cannot exceed 100")
    private BigDecimal commissionRate;

    @Positive(message = "Base salary must be positive")
    private BigDecimal baseSalary;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String terms;

    private String notes;
}
