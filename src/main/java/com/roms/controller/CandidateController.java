package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.JobApplicationRequest;
import com.roms.entity.Candidate;
import com.roms.enums.CandidateStatus;
import com.roms.repository.CandidateRepository;
import com.roms.service.CandidateWorkflowService;
import com.roms.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateWorkflowService workflowService;

    @Autowired
    private JobApplicationService jobApplicationService;

    /**
     * Public endpoint for job applications
     * Creates both user account and candidate record in one transaction
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyForJob(@Valid @RequestBody JobApplicationRequest request) {
        try {
            Candidate candidate = jobApplicationService.applyForJob(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Application submitted successfully! You can now login to track your status.", candidate));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Application failed: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<?> getAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAllActive();
        return ResponseEntity.ok(ApiResponse.success("Candidates retrieved successfully", candidates));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<?> getMyApplications(@RequestParam String email) {
        List<Candidate> candidates = candidateRepository.findAllByEmailAndDeletedAtIsNull(email);
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", candidates));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<?> getCandidateById(@PathVariable Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success("Candidate retrieved successfully", candidate));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> createCandidate(@RequestBody Candidate candidate) {
        // Check for duplicate passport
        if (candidateRepository.existsByPassportNoAndDeletedAtIsNull(candidate.getPassportNo())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Candidate with this passport number already exists"));
        }

        candidate.setCurrentStatus(CandidateStatus.APPLIED);
        Candidate savedCandidate = candidateRepository.save(candidate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate created successfully", savedCandidate));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidateDetails) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));

        // Update basic fields
        if (candidateDetails.getFirstName() != null) {
            candidate.setFirstName(candidateDetails.getFirstName());
        }
        if (candidateDetails.getMiddleName() != null) {
            candidate.setMiddleName(candidateDetails.getMiddleName());
        }
        if (candidateDetails.getLastName() != null) {
            candidate.setLastName(candidateDetails.getLastName());
        }
        if (candidateDetails.getEmail() != null) {
            candidate.setEmail(candidateDetails.getEmail());
        }
        if (candidateDetails.getPhoneNumber() != null) {
            candidate.setPhoneNumber(candidateDetails.getPhoneNumber());
        }
        if (candidateDetails.getAddress() != null) {
            candidate.setAddress(candidateDetails.getAddress());
        }
        
        // Update interview fields
        if (candidateDetails.getInterviewDate() != null) {
            candidate.setInterviewDate(candidateDetails.getInterviewDate());
        }
        if (candidateDetails.getInterviewTime() != null) {
            candidate.setInterviewTime(candidateDetails.getInterviewTime());
        }
        if (candidateDetails.getInterviewLocation() != null) {
            candidate.setInterviewLocation(candidateDetails.getInterviewLocation());
        }
        if (candidateDetails.getInterviewNotes() != null) {
            candidate.setInterviewNotes(candidateDetails.getInterviewNotes());
        }

        Candidate updatedCandidate = candidateRepository.save(candidate);
        return ResponseEntity.ok(ApiResponse.success("Candidate updated successfully", updatedCandidate));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));

        candidate.softDelete();
        candidateRepository.save(candidate);

        return ResponseEntity.ok(ApiResponse.success("Candidate deleted successfully"));
    }

    @PostMapping("/{id}/transition")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> transitionStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            CandidateStatus newStatus = CandidateStatus.valueOf(request.get("status"));
            Candidate candidate = workflowService.transitionStatus(id, newStatus);
            return ResponseEntity.ok(ApiResponse.success("Status transitioned successfully", candidate));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Transition failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/can-transition/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> canTransition(@PathVariable Long id, @PathVariable String status) {
        try {
            CandidateStatus newStatus = CandidateStatus.valueOf(status);
            boolean canTransition = workflowService.canTransition(id, newStatus);
            String reason = canTransition ? null : workflowService.getTransitionBlockReason(id, newStatus);

            return ResponseEntity.ok(ApiResponse.success("Transition check complete", 
                    Map.of("canTransition", canTransition, "reason", reason)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Check failed: " + e.getMessage()));
        }
    }
}
