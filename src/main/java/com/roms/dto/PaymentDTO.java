package com.roms.dto;

import com.roms.enums.PaymentType;
import com.roms.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Payment with transaction details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    
    private Long id;
    
    private Long candidateId;
    
    private Long assignmentId;
    
    private UUID agreementId;
    
    private BigDecimal amount;
    
    private PaymentType type;
    
    private TransactionType transactionType;
    
    private String transactionRef;
    
    private LocalDateTime paymentDate;
    
    private String paymentMethod;
    
    private String mpesaRef;
    
    private String description;
    
    private Boolean isReversal;
    
    private Long linkedTransactionId;
    
    private String reversalReason;
}
