package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.CommissionAgreementDTO;
import com.roms.dto.CreateCommissionAgreementRequest;
import com.roms.service.AgencyCommissionAgreementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commission-agreements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommissionAgreementController {

    private final AgencyCommissionAgreementService commissionAgreementService;

    /**
     * Create a new commission agreement
     * Only SUPER_ADMIN and OPERATIONS_STAFF can create agreements
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> createAgreement(@Valid @RequestBody CreateCommissionAgreementRequest request) {
        try {
            CommissionAgreementDTO agreement = commissionAgreementService.createAgreement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Commission agreement created successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get a specific commission agreement
     */
    @GetMapping("/{agreementId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> getAgreement(@PathVariable Long agreementId) {
        try {
            CommissionAgreementDTO agreement = commissionAgreementService.getAgreement(agreementId);
            return ResponseEntity.ok(ApiResponse.success("Agreement retrieved successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all agreements for a candidate
     */
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> getCandidateAgreements(@PathVariable Long candidateId) {
        try {
            List<CommissionAgreementDTO> agreements = commissionAgreementService.getCandidateAgreements(candidateId);
            return ResponseEntity.ok(ApiResponse.success("Candidate agreements retrieved successfully", agreements));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get agreement by assignment
     */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF')")
    public ResponseEntity<ApiResponse> getAssignmentAgreement(@PathVariable Long assignmentId) {
        try {
            CommissionAgreementDTO agreement = commissionAgreementService.getAssignmentAgreement(assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Assignment agreement retrieved successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Sign a commission agreement
     */
    @PutMapping("/{agreementId}/sign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> signAgreement(@PathVariable Long agreementId) {
        try {
            CommissionAgreementDTO agreement = commissionAgreementService.signAgreement(agreementId);
            return ResponseEntity.ok(ApiResponse.success("Agreement signed successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cancel a commission agreement
     */
    @DeleteMapping("/{agreementId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> cancelAgreement(
            @PathVariable Long agreementId,
            @RequestParam(required = false) String reason) {
        try {
            String cancellationReason = reason != null ? reason : "No reason provided";
            CommissionAgreementDTO agreement = commissionAgreementService.cancelAgreement(agreementId, cancellationReason);
            return ResponseEntity.ok(ApiResponse.success("Agreement cancelled successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
