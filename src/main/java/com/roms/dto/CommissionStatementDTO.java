package com.roms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Financial statement for candidate's commission payments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionStatementDTO {
    
    private Long candidateId;
    
    private String candidateName;
    
    private BigDecimal totalCommissionAmount;
    
    private BigDecimal requiredDownpaymentAmount;
    
    private BigDecimal totalPaid;
    
    private BigDecimal outstandingBalance;
    
    private Boolean downpaymentComplete;
    
    private Boolean fullPaymentComplete;
    
    private List<PaymentDTO> paymentHistory;
}
