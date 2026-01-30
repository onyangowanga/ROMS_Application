package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.CommissionPaymentRequest;
import com.roms.dto.CommissionStatementDTO;
import com.roms.dto.PaymentDTO;
import com.roms.service.CommissionPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for Commission Payment operations
 * Phase 2B: Immutable ledger for applicant commission payments
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommissionPaymentController {

    private final CommissionPaymentService paymentService;

    /**
     * Record downpayment
     * OPERATIONS_STAFF and SUPER_ADMIN only
     */
    @PostMapping("/downpayment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> recordDownpayment(
            @RequestBody CommissionPaymentRequest request) {
        try {
            PaymentDTO payment = paymentService.recordDownpayment(request);
            return ResponseEntity.ok(ApiResponse.success("Downpayment recorded successfully", payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Record installment payment
     * OPERATIONS_STAFF and SUPER_ADMIN only
     */
    @PostMapping("/installment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> recordInstallment(
            @RequestBody CommissionPaymentRequest request) {
        try {
            PaymentDTO payment = paymentService.recordInstallment(request);
            return ResponseEntity.ok(ApiResponse.success("Installment recorded successfully", payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Reverse a payment (error correction)
     * SUPER_ADMIN only - creates negative entry, original never modified
     */
    @PostMapping("/{paymentId}/reverse")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> reversePayment(
            @PathVariable Long paymentId,
            @RequestParam String reason) {
        try {
            PaymentDTO reversal = paymentService.reversePayment(paymentId, reason);
            return ResponseEntity.ok(ApiResponse.success("Payment reversed successfully", reversal));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get payment statement for candidate
     * Shows all commission payments, balances, and workflow status
     */
    @GetMapping("/candidate/{candidateId}/statement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> getCandidateStatement(
            @PathVariable Long candidateId,
            @RequestParam UUID agreementId) {
        try {
            CommissionStatementDTO statement = paymentService.getCandidateStatement(candidateId, agreementId);
            return ResponseEntity.ok(ApiResponse.success("Commission statement retrieved", statement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if downpayment is complete for assignment
     * Used by frontend to show workflow locks
     */
    @GetMapping("/assignment/{assignmentId}/downpayment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> checkDownpaymentStatus(@PathVariable Long assignmentId) {
        try {
            boolean isComplete = paymentService.isDownpaymentComplete(assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Downpayment status retrieved", isComplete));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if full payment is complete for assignment
     * Used by frontend to show workflow locks
     */
    @GetMapping("/assignment/{assignmentId}/fullpayment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> checkFullPaymentStatus(@PathVariable Long assignmentId) {
        try {
            boolean isComplete = paymentService.isFullPaymentComplete(assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Full payment status retrieved", isComplete));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
