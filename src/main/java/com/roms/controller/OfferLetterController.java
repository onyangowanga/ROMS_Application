package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.entity.OfferLetter;
import com.roms.service.OfferLetterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for offer letter management
 * Handles the complete offer lifecycle: draft → issue → sign
 */
@RestController
@RequestMapping("/api/offers")
@Slf4j
public class OfferLetterController {

    @Autowired
    private OfferLetterService offerLetterService;

    /**
     * Create a draft offer letter
     * Accessible by operations staff and admins
     */
    @PostMapping("/draft")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> createDraft(
            @RequestParam Long candidateId,
            @RequestParam Long jobOrderId,
            @RequestParam Double salary,
            @RequestParam String jobTitle) {

        OfferLetter draft = offerLetterService.createDraft(candidateId, jobOrderId, salary, jobTitle);
        return ResponseEntity.ok(ApiResponse.success("Draft offer letter created", draft));
    }

    /**
     * Issue an offer letter to candidate
     * CRITICAL: Requires medical clearance (enforced in service layer)
     * Accessible by operations staff and admins
     */
    @PostMapping("/{offerLetterId}/issue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> issueOffer(@PathVariable Long offerLetterId) {
        
        OfferLetter issued = offerLetterService.issueOffer(offerLetterId);
        return ResponseEntity.ok(ApiResponse.success("Offer letter issued successfully", issued));
    }

    /**
     * Sign an offer letter
     * CRITICAL: Only APPLICANT role can sign their own offer
     */
    @PostMapping("/{offerLetterId}/sign")
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<ApiResponse> signOffer(
            @PathVariable Long offerLetterId,
            Authentication authentication) {

        String username = authentication.getName();
        OfferLetter signed = offerLetterService.signOffer(offerLetterId, username);
        
        return ResponseEntity.ok(ApiResponse.success("Offer letter signed successfully", signed));
    }

    /**
     * Withdraw an offer letter
     * Accessible by admins and operations staff
     */
    @PostMapping("/{offerLetterId}/withdraw")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> withdrawOffer(
            @PathVariable Long offerLetterId,
            @RequestParam String reason) {

        OfferLetter withdrawn = offerLetterService.withdrawOffer(offerLetterId, reason);
        return ResponseEntity.ok(ApiResponse.success("Offer letter withdrawn", withdrawn));
    }

    /**
     * Get all offer letters for a candidate
     */
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<ApiResponse> getCandidateOffers(@PathVariable Long candidateId) {
        
        List<OfferLetter> offers = offerLetterService.getCandidateOffers(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate offers retrieved", offers));
    }

    /**
     * Get all offer letters for a job order
     */
    @GetMapping("/job-order/{jobOrderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'EMPLOYER')")
    public ResponseEntity<ApiResponse> getJobOrderOffers(@PathVariable Long jobOrderId) {
        
        List<OfferLetter> offers = offerLetterService.getJobOrderOffers(jobOrderId);
        return ResponseEntity.ok(ApiResponse.success("Job order offers retrieved", offers));
    }

    /**
     * Check if candidate can receive a new offer
     * Prevents multiple concurrent offers
     */
    @GetMapping("/candidate/{candidateId}/can-receive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> canReceiveOffer(@PathVariable Long candidateId) {
        
        boolean canReceive = offerLetterService.canReceiveOffer(candidateId);
        return ResponseEntity.ok(ApiResponse.success(
                canReceive ? "Candidate can receive offer" : "Candidate has pending offer", 
                canReceive));
    }
}
