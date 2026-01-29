package com.roms.service;

import com.roms.dto.AssignmentDTO;
import com.roms.dto.CreateAssignmentRequest;
import com.roms.entity.Assignment;
import com.roms.entity.Candidate;
import com.roms.entity.JobOrder;
import com.roms.enums.AssignmentStatus;
import com.roms.enums.JobOrderStatus;
import com.roms.repository.AssignmentRepository;
import com.roms.repository.CandidateRepository;
import com.roms.repository.JobOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CandidateRepository candidateRepository;
    private final JobOrderRepository jobOrderRepository;

    /**
     * Create a new assignment with business rule validation
     */
    @Transactional
    public AssignmentDTO createAssignment(CreateAssignmentRequest request) {
        // Validate candidate exists
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + request.getCandidateId()));

        // Validate job order exists
        JobOrder jobOrder = jobOrderRepository.findById(request.getJobOrderId())
                .orElseThrow(() -> new RuntimeException("Job order not found with id: " + request.getJobOrderId()));

        // Business Rule 1: Candidate cannot have more than one active assignment
        if (assignmentRepository.hasActiveAssignment(candidate.getId())) {
            throw new RuntimeException("Candidate already has an active assignment. Only one active assignment is allowed.");
        }

        // Business Rule 2: Job order must be OPEN
        if (jobOrder.getStatus() != JobOrderStatus.OPEN) {
            throw new RuntimeException("Cannot assign to job order. Job status must be OPEN, current status: " + jobOrder.getStatus());
        }

        // Business Rule 3: Job order must have available headcount
        long currentAssignments = assignmentRepository.countActiveByJobOrderId(jobOrder.getId());
        if (currentAssignments >= jobOrder.getHeadcountRequired()) {
            throw new RuntimeException("Cannot assign to job order. Job is already full (" + 
                    currentAssignments + "/" + jobOrder.getHeadcountRequired() + ")");
        }

        // Create assignment
        Assignment assignment = Assignment.builder()
                .candidate(candidate)
                .jobOrder(jobOrder)
                .status(AssignmentStatus.ASSIGNED)
                .isActive(true)
                .assignedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        assignment = assignmentRepository.save(assignment);

        // Update job order headcount
        jobOrder.setHeadcountFilled((int) currentAssignments + 1);
        jobOrderRepository.save(jobOrder);

        return toDTO(assignment);
    }

    /**
     * Get all assignments for a job order
     */
    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByJobOrder(Long jobOrderId) {
        return assignmentRepository.findByJobOrderId(jobOrderId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all assignments for a candidate
     */
    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByCandidate(Long candidateId) {
        return assignmentRepository.findByCandidateId(candidateId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active assignment for a candidate
     */
    @Transactional(readOnly = true)
    public AssignmentDTO getActiveAssignment(Long candidateId) {
        List<Assignment> activeAssignments = assignmentRepository.findActiveByCandidateId(candidateId);
        
        // If multiple active assignments exist (data integrity issue), return the most recent
        if (activeAssignments.isEmpty()) {
            return null;
        }
        
        // Log warning if multiple active assignments found
        if (activeAssignments.size() > 1) {
            System.err.println("WARNING: Candidate " + candidateId + " has " + activeAssignments.size() + 
                    " active assignments. Only one should exist. Returning the most recent.");
        }
        
        return toDTO(activeAssignments.get(0));
    }

    /**
     * Check if candidate has active assignment
     */
    @Transactional(readOnly = true)
    public boolean hasActiveAssignment(Long candidateId) {
        return assignmentRepository.hasActiveAssignment(candidateId);
    }

    /**
     * Cancel an assignment
     */
    @Transactional
    public void cancelAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (!assignment.getIsActive()) {
            throw new RuntimeException("Assignment is already inactive");
        }

        // Deactivate assignment
        assignment.deactivate();
        assignmentRepository.save(assignment);

        // Update job order headcount
        JobOrder jobOrder = assignment.getJobOrder();
        long activeAssignments = assignmentRepository.countActiveByJobOrderId(jobOrder.getId());
        jobOrder.setHeadcountFilled((int) activeAssignments);
        jobOrderRepository.save(jobOrder);
    }

    /**
     * Issue offer for an assignment
     */
    @Transactional
    public AssignmentDTO issueOffer(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (!assignment.getIsActive()) {
            throw new RuntimeException("Cannot issue offer for inactive assignment");
        }

        assignment.issueOffer();
        assignment = assignmentRepository.save(assignment);
        return toDTO(assignment);
    }

    /**
     * Confirm placement for an assignment
     */
    @Transactional
    public AssignmentDTO confirmPlacement(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (!assignment.getIsActive()) {
            throw new RuntimeException("Cannot confirm placement for inactive assignment");
        }

        if (assignment.getOfferIssuedAt() == null) {
            throw new RuntimeException("Cannot confirm placement before offer is issued");
        }

        assignment.confirmPlacement();
        assignment = assignmentRepository.save(assignment);
        return toDTO(assignment);
    }

    /**
     * Get all assignments
     */
    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private AssignmentDTO toDTO(Assignment assignment) {
        return AssignmentDTO.builder()
                .id(assignment.getId())
                .candidateId(assignment.getCandidate().getId())
                .candidateName(assignment.getCandidate().getFirstName() + " " + assignment.getCandidate().getLastName())
                .candidateRefNo(assignment.getCandidate().getInternalRefNo())
                .jobOrderId(assignment.getJobOrder().getId())
                .jobOrderRef(assignment.getJobOrder().getJobOrderRef())
                .jobTitle(assignment.getJobOrder().getJobTitle())
                .status(assignment.getStatus())
                .isActive(assignment.getIsActive())
                .assignedAt(assignment.getAssignedAt())
                .offerIssuedAt(assignment.getOfferIssuedAt())
                .placementConfirmedAt(assignment.getPlacementConfirmedAt())
                .cancelledAt(assignment.getCancelledAt())
                .notes(assignment.getNotes())
                .build();
    }
}
