package com.roms.service;

import com.roms.dto.CommissionAgreementDTO;
import com.roms.dto.CreateCommissionAgreementRequest;
import com.roms.entity.AgencyCommissionAgreement;
import com.roms.entity.Assignment;
import com.roms.entity.Candidate;
import com.roms.enums.AgreementStatus;
import com.roms.repository.AgencyCommissionAgreementRepository;
import com.roms.repository.AssignmentRepository;
import com.roms.repository.CandidateRepository;
import com.roms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Agency Commission Agreements
 * 
 * Business Rules:
 * - One active agreement per Assignment
 * - Amounts immutable once signed
 * - Agreement required before accepting payments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgencyCommissionAgreementService {

    private final AgencyCommissionAgreementRepository agreementRepository;
    private final AssignmentRepository assignmentRepository;
    private final CandidateRepository candidateRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Create new commission agreement
     * OPERATIONS_STAFF only
     */
    @Transactional
    public CommissionAgreementDTO createAgreement(CreateCommissionAgreementRequest request) {
        log.info("Creating commission agreement for candidate {} assignment {}", 
                 request.getCandidateId(), request.getAssignmentId());

        // Validate candidate exists
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // Validate assignment exists
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Check no active agreement exists for this assignment
        agreementRepository.findActiveByAssignmentId(request.getAssignmentId())
                .ifPresent(existing -> {
                    throw new RuntimeException("Active agreement already exists for this assignment");
                });

        // Validate amounts
        if (request.getTotalCommissionAmount() == null || 
            request.getTotalCommissionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total commission amount must be greater than zero");
        }

        if (request.getRequiredDownpaymentAmount() == null || 
            request.getRequiredDownpaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Required downpayment amount must be greater than zero");
        }

        if (request.getRequiredDownpaymentAmount().compareTo(request.getTotalCommissionAmount()) > 0) {
            throw new RuntimeException("Downpayment cannot exceed total commission");
        }

        // Create agreement
        AgencyCommissionAgreement agreement = AgencyCommissionAgreement.builder()
                .candidate(candidate)
                .assignment(assignment)
                .totalCommissionAmount(request.getTotalCommissionAmount())
                .requiredDownpaymentAmount(request.getRequiredDownpaymentAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "KES")
                .status(AgreementStatus.ACTIVE)
                .signed(false)
                .notes(request.getNotes())
                .build();

        agreement = agreementRepository.save(agreement);
        log.info("Created commission agreement: {}", agreement.getId());

        return toDTO(agreement);
    }

    /**
     * Get all commission agreements
     * For finance dashboard
     */
    @Transactional(readOnly = true)
    public List<CommissionAgreementDTO> getAllAgreements() {
        return agreementRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get agreement by ID
     */
    @Transactional(readOnly = true)
    public CommissionAgreementDTO getAgreement(UUID agreementId) {
        AgencyCommissionAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        return toDTO(agreement);
    }

    /**
     * Get all agreements for a candidate
     */
    @Transactional(readOnly = true)
    public List<CommissionAgreementDTO> getCandidateAgreements(Long candidateId) {
        return agreementRepository.findByCandidateId(candidateId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active agreement for assignment
     */
    @Transactional(readOnly = true)
    public CommissionAgreementDTO getActiveAgreementForAssignment(Long assignmentId) {
        return agreementRepository.findActiveByAssignmentId(assignmentId)
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Sign agreement (by applicant or staff)
     * Once signed, amounts become immutable
     */
    @Transactional
    public CommissionAgreementDTO signAgreement(UUID agreementId, String documentUrl) {
        AgencyCommissionAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        if (agreement.getSigned()) {
            throw new RuntimeException("Agreement already signed");
        }

        agreement.sign();
        if (documentUrl != null) {
            agreement.setSignedAgreementDocumentUrl(documentUrl);
        }

        agreement = agreementRepository.save(agreement);
        log.info("Agreement {} signed", agreementId);

        return toDTO(agreement);
    }

    /**
     * Cancel agreement (SUPER_ADMIN only)
     */
    @Transactional
    public void cancelAgreement(UUID agreementId, String reason) {
        AgencyCommissionAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        if (agreement.getStatus() == AgreementStatus.CANCELLED) {
            throw new RuntimeException("Agreement already cancelled");
        }

        agreement.cancel(reason);
        agreementRepository.save(agreement);
        log.warn("Agreement {} cancelled: {}", agreementId, reason);
    }

    /**
     * Mark agreement as completed (when full payment received)
     * Called by CommissionPaymentService
     */
    @Transactional
    public void completeAgreement(UUID agreementId) {
        AgencyCommissionAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        agreement.complete();
        agreementRepository.save(agreement);
        log.info("Agreement {} marked as completed", agreementId);
    }

    /**
     * Convert entity to DTO with calculated payment info
     */
    private CommissionAgreementDTO toDTO(AgencyCommissionAgreement agreement) {
        BigDecimal totalPaid = paymentRepository.calculateTotalPaidForAgreement(agreement.getId());
        BigDecimal outstandingBalance = agreement.getTotalCommissionAmount().subtract(totalPaid);

        return CommissionAgreementDTO.builder()
                .id(agreement.getId())
                .candidateId(agreement.getCandidate().getId())
                .candidateName(agreement.getCandidate().getFirstName() + " " + 
                              agreement.getCandidate().getLastName())
                .assignmentId(agreement.getAssignment().getId())
                .totalCommissionAmount(agreement.getTotalCommissionAmount())
                .requiredDownpaymentAmount(agreement.getRequiredDownpaymentAmount())
                .totalPaid(totalPaid)
                .outstandingBalance(outstandingBalance)
                .currency(agreement.getCurrency())
                .agreementDate(agreement.getAgreementDate())
                .signed(agreement.getSigned())
                .signedAt(agreement.getSignedAt())
                .signedAgreementDocumentUrl(agreement.getSignedAgreementDocumentUrl())
                .status(agreement.getStatus())
                .notes(agreement.getNotes())
                .createdAt(agreement.getCreatedAt())
                .createdBy(agreement.getCreatedBy())
                .build();
    }
}
