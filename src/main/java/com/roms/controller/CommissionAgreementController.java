package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.CommissionAgreementDTO;
import com.roms.dto.CreateCommissionAgreementRequest;
import com.roms.service.AgencyCommissionAgreementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Agency Commission Agreement management
 * Phase 2B: Applicant-funded commission system
 */
@RestController
@RequestMapping("/api/agreements")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommissionAgreementController {

    private final AgencyCommissionAgreementService agreementService;

    /**
     * Get all commission agreements
     * Finance dashboard - shows all candidates with commissions
     * FINANCE_MANAGER and SUPER_ADMIN only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> getAllAgreements() {
        try {
            List<CommissionAgreementDTO> agreements = agreementService.getAllAgreements();
            return ResponseEntity.ok(ApiResponse.success("All agreements retrieved", agreements));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create new commission agreement
     * OPERATIONS_STAFF and SUPER_ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> createAgreement(
            @RequestBody CreateCommissionAgreementRequest request) {
        try {
            CommissionAgreementDTO agreement = agreementService.createAgreement(request);
            return ResponseEntity.ok(ApiResponse.success("Commission agreement created successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get agreement by ID
     * RBAC: SUPER_ADMIN, OPERATIONS_STAFF can view all; APPLICANT can view own
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> getAgreement(@PathVariable UUID id) {
        try {
            CommissionAgreementDTO agreement = agreementService.getAgreement(id);
            return ResponseEntity.ok(ApiResponse.success("Agreement retrieved", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all agreements for a candidate
     */
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> getCandidateAgreements(
            @PathVariable Long candidateId) {
        try {
            List<CommissionAgreementDTO> agreements = agreementService.getCandidateAgreements(candidateId);
            return ResponseEntity.ok(ApiResponse.success("Agreements retrieved", agreements));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get active agreement for assignment
     */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> getAssignmentAgreement(
            @PathVariable Long assignmentId) {
        try {
            CommissionAgreementDTO agreement = agreementService.getActiveAgreementForAssignment(assignmentId);
            if (agreement == null) {
                return ResponseEntity.ok(ApiResponse.success("No active agreement found", null));
            }
            return ResponseEntity.ok(ApiResponse.success("Agreement retrieved", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Sign agreement
     * Can be done by APPLICANT or STAFF
     */
    @PutMapping("/{id}/sign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> signAgreement(
            @PathVariable UUID id,
            @RequestParam(required = false) String documentUrl) {
        try {
            CommissionAgreementDTO agreement = agreementService.signAgreement(id, documentUrl);
            return ResponseEntity.ok(ApiResponse.success("Agreement signed successfully", agreement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cancel agreement
     * SUPER_ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> cancelAgreement(
            @PathVariable UUID id,
            @RequestParam String reason) {
        try {
            agreementService.cancelAgreement(id, reason);
            return ResponseEntity.ok(ApiResponse.success("Agreement cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
