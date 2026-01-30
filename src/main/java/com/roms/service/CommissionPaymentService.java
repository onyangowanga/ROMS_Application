package com.roms.service;

import com.roms.dto.CommissionPaymentRequest;
import com.roms.dto.CommissionStatementDTO;
import com.roms.dto.PaymentDTO;
import com.roms.entity.AgencyCommissionAgreement;
import com.roms.entity.Payment;
import com.roms.enums.AgreementStatus;
import com.roms.enums.PaymentType;
import com.roms.enums.TransactionType;
import com.roms.repository.AgencyCommissionAgreementRepository;
import com.roms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing commission payments
 * 
 * CRITICAL BUSINESS RULES:
 * - First payment must be >= requiredDownpaymentAmount
 * - All payments recorded as immutable ledger entries
 * - No updates or deletes - reversals only
 * - BigDecimal for all monetary calculations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionPaymentService {

    private final PaymentRepository paymentRepository;
    private final AgencyCommissionAgreementRepository agreementRepository;
    private final AgencyCommissionAgreementService agreementService;

    /**
     * Record downpayment
     * MUST be >= requiredDownpaymentAmount
     * MUST be first payment for agreement
     */
    @Transactional
    public PaymentDTO recordDownpayment(CommissionPaymentRequest request) {
        log.info("Recording downpayment for agreement: {}", request.getAgreementId());

        AgencyCommissionAgreement agreement = agreementRepository.findById(request.getAgreementId())
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        // Validate agreement is active
        if (agreement.getStatus() != AgreementStatus.ACTIVE) {
            throw new RuntimeException("Agreement is not active");
        }

        // Calculate total downpayments already made
        List<Payment> existingDownpayments = paymentRepository.findByAgreementIdAndTransactionType(
                agreement.getId(), TransactionType.AGENCY_COMMISSION_DOWNPAYMENT);
        
        BigDecimal totalDownpaymentsMade = existingDownpayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal newTotal = totalDownpaymentsMade.add(request.getAmount());
        
        // Validate total downpayments don't exceed required amount
        if (newTotal.compareTo(agreement.getRequiredDownpaymentAmount()) > 0) {
            BigDecimal remaining = agreement.getRequiredDownpaymentAmount().subtract(totalDownpaymentsMade);
            throw new RuntimeException(String.format(
                    "Downpayment installment exceeds remaining requirement. Remaining: %s %s",
                    agreement.getCurrency(),
                    remaining));
        }

        // Create payment record
        Payment payment = Payment.builder()
                .candidate(agreement.getCandidate())
                .assignment(agreement.getAssignment())
                .agreement(agreement)
                .amount(request.getAmount())
                .type(PaymentType.DEBIT)
                .transactionType(TransactionType.AGENCY_COMMISSION_DOWNPAYMENT)
                .paymentMethod(request.getPaymentMethod())
                .mpesaRef(request.getMpesaRef())
                .description(request.getDescription() != null ? 
                            request.getDescription() : 
                            "Agency commission downpayment")
                .isReversal(false)
                .paymentDate(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Downpayment recorded: {} for agreement {}", payment.getId(), agreement.getId());

        return toDTO(payment);
    }

    /**
     * Record installment payment
     * Can be multiple installments
     */
    @Transactional
    public PaymentDTO recordInstallment(CommissionPaymentRequest request) {
        log.info("Recording installment for agreement: {}", request.getAgreementId());

        AgencyCommissionAgreement agreement = agreementRepository.findById(request.getAgreementId())
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        // Validate agreement is active
        if (agreement.getStatus() != AgreementStatus.ACTIVE) {
            throw new RuntimeException("Agreement is not active");
        }

        // Check downpayment exists
        List<Payment> downpayments = paymentRepository.findByAgreementIdAndTransactionType(
                agreement.getId(), TransactionType.AGENCY_COMMISSION_DOWNPAYMENT);
        
        if (downpayments.isEmpty()) {
            throw new RuntimeException("Downpayment must be recorded before installments");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Installment amount must be greater than zero");
        }

        // Calculate current total
        BigDecimal totalPaid = paymentRepository.calculateTotalPaidForAgreement(agreement.getId());
        BigDecimal afterPayment = totalPaid.add(request.getAmount());

        // Check if overpayment
        if (afterPayment.compareTo(agreement.getTotalCommissionAmount()) > 0) {
            throw new RuntimeException(String.format(
                    "Payment would exceed total commission. Outstanding: %s, Payment: %s",
                    agreement.getTotalCommissionAmount().subtract(totalPaid),
                    request.getAmount()));
        }

        // Create payment record
        Payment payment = Payment.builder()
                .candidate(agreement.getCandidate())
                .assignment(agreement.getAssignment())
                .agreement(agreement)
                .amount(request.getAmount())
                .type(PaymentType.DEBIT)
                .transactionType(TransactionType.AGENCY_COMMISSION_INSTALLMENT)
                .paymentMethod(request.getPaymentMethod())
                .mpesaRef(request.getMpesaRef())
                .description(request.getDescription() != null ? 
                            request.getDescription() : 
                            "Agency commission installment")
                .isReversal(false)
                .paymentDate(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Installment recorded: {} for agreement {}", payment.getId(), agreement.getId());

        // Check if fully paid
        if (afterPayment.compareTo(agreement.getTotalCommissionAmount()) == 0) {
            agreementService.completeAgreement(agreement.getId());
            log.info("Agreement {} fully paid and completed", agreement.getId());
        }

        return toDTO(payment);
    }

    /**
     * Reverse a payment (error correction)
     * Creates negative entry, original transaction never modified
     */
    @Transactional
    public PaymentDTO reversePayment(Long paymentId, String reason) {
        log.warn("Reversing payment: {} - Reason: {}", paymentId, reason);

        Payment originalPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Check if already reversed
        if (originalPayment.getIsReversal()) {
            throw new RuntimeException("Cannot reverse a reversal transaction");
        }

        // Check if already has reversal
        List<Payment> existingReversals = paymentRepository.findByAgreementId(
                originalPayment.getAgreement().getId()).stream()
                .filter(p -> p.getLinkedTransactionId() != null && 
                            p.getLinkedTransactionId().equals(paymentId))
                .collect(Collectors.toList());

        if (!existingReversals.isEmpty()) {
            throw new RuntimeException("Payment already reversed");
        }

        // Create reversal
        Payment reversalPayment = originalPayment.createReversal(reason, "SYSTEM"); // Should get from SecurityContext
        reversalPayment = paymentRepository.save(reversalPayment);

        log.info("Reversal created: {} for original payment {}", reversalPayment.getId(), paymentId);

        return toDTO(reversalPayment);
    }

    /**
     * Get payment statement for candidate
     * Shows all commission payments and current status
     */
    @Transactional(readOnly = true)
    public CommissionStatementDTO getCandidateStatement(Long candidateId, UUID agreementId) {
        AgencyCommissionAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        if (!agreement.getCandidate().getId().equals(candidateId)) {
            throw new RuntimeException("Agreement does not belong to this candidate");
        }

        BigDecimal totalPaid = paymentRepository.calculateTotalPaidForAgreement(agreementId);
        BigDecimal outstandingBalance = agreement.getTotalCommissionAmount().subtract(totalPaid);

        List<Payment> payments = paymentRepository.findByAgreementId(agreementId);
        List<PaymentDTO> paymentDTOs = payments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return CommissionStatementDTO.builder()
                .candidateId(candidateId)
                .candidateName(agreement.getCandidate().getFirstName() + " " + 
                              agreement.getCandidate().getLastName())
                .totalCommissionAmount(agreement.getTotalCommissionAmount())
                .requiredDownpaymentAmount(agreement.getRequiredDownpaymentAmount())
                .totalPaid(totalPaid)
                .outstandingBalance(outstandingBalance)
                .downpaymentComplete(totalPaid.compareTo(agreement.getRequiredDownpaymentAmount()) >= 0)
                .fullPaymentComplete(totalPaid.compareTo(agreement.getTotalCommissionAmount()) >= 0)
                .paymentHistory(paymentDTOs)
                .build();
    }

    /**
     * Check if downpayment is complete for assignment
     * Used by workflow guards
     */
    @Transactional(readOnly = true)
    public boolean isDownpaymentComplete(Long assignmentId) {
        return agreementRepository.findActiveByAssignmentId(assignmentId)
                .map(agreement -> {
                    BigDecimal totalPaid = paymentRepository.calculateTotalPaidForAgreement(agreement.getId());
                    return totalPaid.compareTo(agreement.getRequiredDownpaymentAmount()) >= 0;
                })
                .orElse(false);
    }

    /**
     * Check if full payment is complete for assignment
     * Used by workflow guards
     */
    @Transactional(readOnly = true)
    public boolean isFullPaymentComplete(Long assignmentId) {
        return agreementRepository.findActiveByAssignmentId(assignmentId)
                .map(agreement -> {
                    BigDecimal totalPaid = paymentRepository.calculateTotalPaidForAgreement(agreement.getId());
                    return totalPaid.compareTo(agreement.getTotalCommissionAmount()) >= 0;
                })
                .orElse(false);
    }

    /**
     * Convert Payment entity to DTO
     */
    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .candidateId(payment.getCandidate() != null ? payment.getCandidate().getId() : null)
                .assignmentId(payment.getAssignment() != null ? payment.getAssignment().getId() : null)
                .agreementId(payment.getAgreement() != null ? payment.getAgreement().getId() : null)
                .amount(payment.getAmount())
                .type(payment.getType())
                .transactionType(payment.getTransactionType())
                .transactionRef(payment.getTransactionRef())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .mpesaRef(payment.getMpesaRef())
                .description(payment.getDescription())
                .isReversal(payment.getIsReversal())
                .linkedTransactionId(payment.getLinkedTransactionId())
                .reversalReason(payment.getReversalReason())
                .build();
    }
}
