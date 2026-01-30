package com.roms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating agency commission agreement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommissionAgreementRequest {
    
    private Long candidateId;
    
    private Long assignmentId;
    
    private BigDecimal totalCommissionAmount;
    
    private BigDecimal requiredDownpaymentAmount;
    
    private String currency;
    
    private String notes;
}
