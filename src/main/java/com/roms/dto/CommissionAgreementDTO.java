package com.roms.dto;

import com.roms.enums.AgreementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Agency Commission Agreement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionAgreementDTO {
    
    private UUID id;
    
    private Long candidateId;
    
    private String candidateName;
    
    private Long assignmentId;
    
    private BigDecimal totalCommissionAmount;
    
    private BigDecimal requiredDownpaymentAmount;
    
    private BigDecimal totalPaid;
    
    private BigDecimal outstandingBalance;
    
    private String currency;
    
    private LocalDateTime agreementDate;
    
    private Boolean signed;
    
    private LocalDateTime signedAt;
    
    private String signedAgreementDocumentUrl;
    
    private AgreementStatus status;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private String createdBy;
}
