package com.roms.service;

import com.roms.entity.Candidate;
import com.roms.entity.CandidateDocument;
import com.roms.entity.JobOrder;
import com.roms.enums.CandidateStatus;
import com.roms.enums.DocumentType;
import com.roms.enums.MedicalStatus;
import com.roms.exception.WorkflowException;
import com.roms.repository.CandidateDocumentRepository;
import com.roms.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Workflow State Machine Service
 * Implements guard logic to prevent process skipping
 * 
 * Canonical State Flow:
 * APPLIED → DOCS_SUBMITTED → INTERVIEWED → MEDICAL_PASSED → OFFER_ISSUED → OFFER_ACCEPTED → PLACED
 */
@Service
public class CandidateWorkflowService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateDocumentRepository documentRepository;

    @Value("${roms.passport.min-validity-months:6}")
    private int passportMinValidityMonths;

    /**
     * Validate and transition candidate to new status
     */
    @Transactional
    public Candidate transitionStatus(Long candidateId, CandidateStatus newStatus) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new WorkflowException("Candidate not found with id: " + candidateId));

        CandidateStatus currentStatus = candidate.getCurrentStatus();

        // Validate transition
        validateTransition(candidate, currentStatus, newStatus);

        // Apply guard logic
        applyGuardLogic(candidate, newStatus);

        // Update status
        candidate.setCurrentStatus(newStatus);

        return candidateRepository.save(candidate);
    }

    /**
     * Validate if transition is allowed
     */
    private void validateTransition(Candidate candidate, CandidateStatus from, CandidateStatus to) {
        // Cannot transition if already in terminal states
        if (from == CandidateStatus.PLACED || from == CandidateStatus.REJECTED || from == CandidateStatus.WITHDRAWN) {
            throw new WorkflowException("Cannot transition from terminal status: " + from);
        }

        // Define valid transitions
        switch (to) {
            case DOCS_SUBMITTED:
                if (from != CandidateStatus.APPLIED) {
                    throw new WorkflowException("Can only submit documents from APPLIED status");
                }
                break;

            case INTERVIEWED:
                if (from != CandidateStatus.DOCS_SUBMITTED) {
                    throw new WorkflowException("Can only interview after documents are submitted");
                }
                break;

            case MEDICAL_PASSED:
                if (from != CandidateStatus.INTERVIEWED) {
                    throw new WorkflowException("Can only pass medical after interview");
                }
                break;

            case OFFER_ISSUED:
                if (from != CandidateStatus.MEDICAL_PASSED) {
                    throw new WorkflowException("Can only issue offer after medical clearance");
                }
                break;

            case OFFER_ACCEPTED:
                if (from != CandidateStatus.OFFER_ISSUED) {
                    throw new WorkflowException("Can only accept offer after it's issued");
                }
                break;

            case PLACED:
                if (from != CandidateStatus.OFFER_ACCEPTED) {
                    throw new WorkflowException("Can only place candidate after offer acceptance");
                }
                break;

            case REJECTED:
            case WITHDRAWN:
                // Can reject or withdraw from any non-terminal status
                break;

            default:
                break;
        }
    }

    /**
     * Apply guard logic rules before transition
     */
    private void applyGuardLogic(Candidate candidate, CandidateStatus newStatus) {
        switch (newStatus) {
            case MEDICAL_PASSED:
                // Document Rule: Passport must be valid for at least 6 months
                validatePassportValidity(candidate);
                // Set medical status
                candidate.setMedicalStatus(MedicalStatus.PASSED);
                break;

            case OFFER_ISSUED:
                // Medical Rule: Cannot issue offer unless medical status is PASSED
                if (candidate.getMedicalStatus() != MedicalStatus.PASSED) {
                    throw new WorkflowException("Cannot issue offer: Medical status must be PASSED");
                }
                break;

            case PLACED:
                // Fulfillment Rule: Check job order capacity
                validateJobOrderCapacity(candidate);
                // Increment job order filled count
                if (candidate.getJobOrder() != null) {
                    candidate.getJobOrder().incrementFilledCount();
                }
                break;

            default:
                break;
        }
    }

    /**
     * Validate passport validity (Document Rule)
     */
    private void validatePassportValidity(Candidate candidate) {
        LocalDate minValidDate = LocalDate.now().plusMonths(passportMinValidityMonths);

        if (candidate.getPassportExpiry() == null) {
            throw new WorkflowException("Passport expiry date is required");
        }

        if (candidate.getPassportExpiry().isBefore(minValidDate)) {
            throw new WorkflowException(
                String.format("Passport must be valid for at least %d months (until %s)", 
                    passportMinValidityMonths, minValidDate)
            );
        }

        // Optionally verify passport document exists
        Optional<CandidateDocument> passportDoc = documentRepository
            .findByCandidateIdAndDocType(candidate.getId(), DocumentType.PASSPORT);

        if (passportDoc.isEmpty()) {
            throw new WorkflowException("Passport document must be uploaded");
        }
    }

    /**
     * Validate job order capacity (Fulfillment Rule)
     */
    private void validateJobOrderCapacity(Candidate candidate) {
        JobOrder jobOrder = candidate.getJobOrder();

        if (jobOrder == null) {
            throw new WorkflowException("Cannot place candidate: No job order assigned");
        }

        if (jobOrder.isFilled()) {
            throw new WorkflowException(
                String.format("Cannot place candidate: Job order %s is already filled (%d/%d)",
                    jobOrder.getJobOrderRef(),
                    jobOrder.getHeadcountFilled(),
                    jobOrder.getHeadcountRequired())
            );
        }
    }

    /**
     * Check if candidate can transition to a specific status
     */
    public boolean canTransition(Long candidateId, CandidateStatus newStatus) {
        try {
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new WorkflowException("Candidate not found"));

            validateTransition(candidate, candidate.getCurrentStatus(), newStatus);
            applyGuardLogic(candidate, newStatus);
            return true;
        } catch (WorkflowException e) {
            return false;
        }
    }

    /**
     * Get reason why transition is not allowed
     */
    public String getTransitionBlockReason(Long candidateId, CandidateStatus newStatus) {
        try {
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new WorkflowException("Candidate not found"));

            validateTransition(candidate, candidate.getCurrentStatus(), newStatus);
            applyGuardLogic(candidate, newStatus);
            return null;
        } catch (WorkflowException e) {
            return e.getMessage();
        }
    }
}
