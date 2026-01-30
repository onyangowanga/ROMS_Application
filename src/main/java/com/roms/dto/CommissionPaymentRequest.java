package com.roms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for recording commission payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionPaymentRequest {
    
    private UUID agreementId;
    
    private BigDecimal amount;
    
    private String paymentMethod;
    
    private String mpesaRef;
    
    private String description;
}
