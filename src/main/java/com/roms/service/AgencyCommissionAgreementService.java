package com.roms.service;

import com.roms.dto.CommissionAgreementDTO;
import com.roms.dto.CreateCommissionAgreementRequest;
import com.roms.entity.Assignment;
import com.roms.entity.Candidate;
import com.roms.entity.CommissionAgreement;
import com.roms.enums.CommissionAgreementStatus;
import com.roms.repository.AssignmentRepository;
import com.roms.repository.CandidateRepository;
import com.roms.repository.CommissionAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyCommissionAgreementService {

    private final CommissionAgreementRepository commissionAgreementRepository;
    private final CandidateRepository candidateRepository;
    private final AssignmentRepository assignmentRepository;

    /**
     * Create a new commission agreement
     */
    @Transactional
    public CommissionAgreementDTO createAgreement(CreateCommissionAgreementRequest request) {
        // Validate candidate exists
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + request.getCandidateId()));

        // Validate assignment if provided
        Assignment assignment = null;
        if (request.getAssignmentId() != null) {
            assignment = assignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + request.getAssignmentId()));
        }

        // Generate unique agreement number
        String agreementNumber = generateAgreementNumber();

        // Create agreement
        CommissionAgreement agreement = CommissionAgreement.builder()
                .candidate(candidate)
                .assignment(assignment)
                .agreementNumber(agreementNumber)
                .commissionRate(request.getCommissionRate())
                .baseSalary(request.getBaseSalary())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .terms(request.getTerms())
                .notes(request.getNotes())
                .status(CommissionAgreementStatus.DRAFT)
                .build();

        agreement = commissionAgreementRepository.save(agreement);
        return toDTO(agreement);
    }

    /**
     * Get agreement by ID
     */
    @Transactional(readOnly = true)
    public CommissionAgreementDTO getAgreement(Long id) {
        CommissionAgreement agreement = commissionAgreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission agreement not found with id: " + id));
        return toDTO(agreement);
    }

    /**
     * Get all agreements for a candidate
     */
    @Transactional(readOnly = true)
    public List<CommissionAgreementDTO> getCandidateAgreements(Long candidateId) {
        return commissionAgreementRepository.findByCandidateId(candidateId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get agreement by assignment
     */
    @Transactional(readOnly = true)
    public CommissionAgreementDTO getAssignmentAgreement(Long assignmentId) {
        CommissionAgreement agreement = commissionAgreementRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() -> new RuntimeException("No commission agreement found for assignment: " + assignmentId));
        return toDTO(agreement);
    }

    /**
     * Sign an agreement
     */
    @Transactional
    public CommissionAgreementDTO signAgreement(Long agreementId) {
        CommissionAgreement agreement = commissionAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Commission agreement not found with id: " + agreementId));

        try {
            agreement.sign();
            agreement = commissionAgreementRepository.save(agreement);
            return toDTO(agreement);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Cancel an agreement
     */
    @Transactional
    public CommissionAgreementDTO cancelAgreement(Long agreementId, String reason) {
        CommissionAgreement agreement = commissionAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Commission agreement not found with id: " + agreementId));

        try {
            agreement.cancel(reason);
            agreement = commissionAgreementRepository.save(agreement);
            return toDTO(agreement);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Generate unique agreement number
     */
    private String generateAgreementNumber() {
        String prefix = "CA-" + LocalDateTime.now().getYear() + "-";
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uniquePart;
    }

    /**
     * Convert entity to DTO
     */
    private CommissionAgreementDTO toDTO(CommissionAgreement agreement) {
        String firstName = agreement.getCandidate().getFirstName() != null ? agreement.getCandidate().getFirstName() : "";
        String lastName = agreement.getCandidate().getLastName() != null ? agreement.getCandidate().getLastName() : "";
        String candidateName = (firstName + " " + lastName).trim();
        
        return CommissionAgreementDTO.builder()
                .id(agreement.getId())
                .candidateId(agreement.getCandidate().getId())
                .candidateName(candidateName)
                .assignmentId(agreement.getAssignment() != null ? agreement.getAssignment().getId() : null)
                .agreementNumber(agreement.getAgreementNumber())
                .commissionRate(agreement.getCommissionRate())
                .baseSalary(agreement.getBaseSalary())
                .startDate(agreement.getStartDate())
                .endDate(agreement.getEndDate())
                .status(agreement.getStatus())
                .signedAt(agreement.getSignedAt())
                .cancelledAt(agreement.getCancelledAt())
                .cancellationReason(agreement.getCancellationReason())
                .terms(agreement.getTerms())
                .notes(agreement.getNotes())
                .createdAt(agreement.getCreatedAt())
                .createdBy(agreement.getCreatedBy())
                .build();
    }
}
