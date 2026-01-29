package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.AssignmentDTO;
import com.roms.dto.CreateAssignmentRequest;
import com.roms.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Create a new assignment
     * Only SUPER_ADMIN and OPERATIONS_STAFF can create assignments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
        try {
            AssignmentDTO assignment = assignmentService.createAssignment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Assignment created successfully", assignment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all assignments
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF')")
    public ResponseEntity<?> getAllAssignments() {
        List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(ApiResponse.success("Assignments retrieved successfully", assignments));
    }

    /**
     * Get assignments for a specific job order
     */
    @GetMapping("/job-order/{jobOrderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF', 'EMPLOYER')")
    public ResponseEntity<?> getAssignmentsByJobOrder(@PathVariable Long jobOrderId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByJobOrder(jobOrderId);
        return ResponseEntity.ok(ApiResponse.success("Job order assignments retrieved successfully", assignments));
    }

    /**
     * Get assignments for a specific candidate
     */
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF')")
    public ResponseEntity<?> getAssignmentsByCandidate(@PathVariable Long candidateId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate assignments retrieved successfully", assignments));
    }

    /**
     * Get active assignment for a candidate
     */
    @GetMapping("/candidate/{candidateId}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'RECRUITMENT_STAFF', 'APPLICANT')")
    public ResponseEntity<?> getActiveAssignment(@PathVariable Long candidateId) {
        AssignmentDTO assignment = assignmentService.getActiveAssignment(candidateId);
        if (assignment == null) {
            return ResponseEntity.ok(ApiResponse.success("No active assignment found", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Active assignment retrieved successfully", assignment));
    }

    /**
     * Cancel an assignment
     */
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> cancelAssignment(@PathVariable Long assignmentId) {
        try {
            assignmentService.cancelAssignment(assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Assignment cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Issue offer for an assignment
     */
    @PutMapping("/{assignmentId}/issue-offer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> issueOffer(@PathVariable Long assignmentId) {
        try {
            AssignmentDTO assignment = assignmentService.issueOffer(assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Offer issued successfully", assignment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Confirm placement for an assignment
     */
    @PutMapping("/{assignmentId}/confirm-placement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> confirmPlacement(@PathVariable Long assignmentId) {
        try {
            AssignmentDTO assignment = assignmentService.confirmPlacement(assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Placement confirmed successfully", assignment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
