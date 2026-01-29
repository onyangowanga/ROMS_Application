package com.roms.dto;

import com.roms.enums.CommissionAgreementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionAgreementDTO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private Long assignmentId;
    private String agreementNumber;
    private BigDecimal commissionRate;
    private BigDecimal baseSalary;
    private LocalDate startDate;
    private LocalDate endDate;
    private CommissionAgreementStatus status;
    private LocalDateTime signedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String terms;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
}
