package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.entity.Candidate;
import com.roms.enums.CandidateStatus;
import com.roms.repository.CandidateRepository;
import com.roms.service.CandidateWorkflowService;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<?> getAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAllActive();
        return ResponseEntity.ok(ApiResponse.success("Candidates retrieved successfully", candidates));
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

        // Update fields
        candidate.setFirstName(candidateDetails.getFirstName());
        candidate.setMiddleName(candidateDetails.getMiddleName());
        candidate.setLastName(candidateDetails.getLastName());
        candidate.setEmail(candidateDetails.getEmail());
        candidate.setPhoneNumber(candidateDetails.getPhoneNumber());
        candidate.setAddress(candidateDetails.getAddress());
        // Add more fields as needed

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
