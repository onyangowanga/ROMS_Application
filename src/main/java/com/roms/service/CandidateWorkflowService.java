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

        // Define valid transitions for 14-stage workflow
        switch (to) {
            case DOCUMENTS_PENDING:
                if (from != CandidateStatus.APPLIED) {
                    throw new WorkflowException("Can only set DOCUMENTS_PENDING from APPLIED status");
                }
                break;

            case DOCUMENTS_UNDER_REVIEW:
                if (from != CandidateStatus.APPLIED && from != CandidateStatus.DOCUMENTS_PENDING) {
                    throw new WorkflowException("Can only review documents from APPLIED or DOCUMENTS_PENDING status");
                }
                break;

            case DOCUMENTS_APPROVED:
                if (from != CandidateStatus.DOCUMENTS_UNDER_REVIEW) {
                    throw new WorkflowException("Can only approve documents after review");
                }
                break;

            case INTERVIEW_SCHEDULED:
                if (from != CandidateStatus.DOCUMENTS_APPROVED) {
                    throw new WorkflowException("Can only schedule interview after documents are approved");
                }
                break;

            case INTERVIEW_COMPLETED:
                if (from != CandidateStatus.INTERVIEW_SCHEDULED) {
                    throw new WorkflowException("Can only complete interview after it's scheduled");
                }
                break;

            case MEDICAL_IN_PROGRESS:
                if (from != CandidateStatus.INTERVIEW_COMPLETED) {
                    throw new WorkflowException("Can only start medical process after interview completion");
                }
                break;

            case MEDICAL_PASSED:
                if (from != CandidateStatus.MEDICAL_IN_PROGRESS) {
                    throw new WorkflowException("Can only pass medical from MEDICAL_IN_PROGRESS status");
                }
                break;

            case OFFER_ISSUED:
                if (from != CandidateStatus.MEDICAL_PASSED) {
                    throw new WorkflowException("Can only issue offer after medical clearance");
                }
                break;

            case OFFER_SIGNED:
                if (from != CandidateStatus.OFFER_ISSUED) {
                    throw new WorkflowException("Can only sign offer after it's issued");
                }
                break;

            case DEPLOYED:
                if (from != CandidateStatus.OFFER_SIGNED) {
                    throw new WorkflowException("Can only deploy after offer is signed");
                }
                break;

            case PLACED:
                if (from != CandidateStatus.DEPLOYED) {
                    throw new WorkflowException("Can only place candidate after deployment");
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
            case DOCUMENTS_UNDER_REVIEW:
                // Ensure required documents are uploaded
                validateRequiredDocuments(candidate);
                break;

            case DOCUMENTS_APPROVED:
                // Ensure passport validity before approval
                validatePassportValidity(candidate);
                break;

            case INTERVIEW_SCHEDULED:
                // Ensure interview details are set
                if (candidate.getInterviewDate() == null) {
                    throw new WorkflowException("Cannot schedule interview: Interview date is required");
                }
                break;

            case MEDICAL_IN_PROGRESS:
                // Set medical status to in progress
                candidate.setMedicalStatus(MedicalStatus.PENDING);
                break;

            case MEDICAL_PASSED:
                // Set medical status to passed
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
     * Validate required documents are uploaded
     */
    private void validateRequiredDocuments(Candidate candidate) {
        // Check for essential documents
        DocumentType[] requiredDocs = {
            DocumentType.PASSPORT,
            DocumentType.CV
        };

        for (DocumentType docType : requiredDocs) {
            Optional<CandidateDocument> doc = documentRepository
                .findByCandidateIdAndDocType(candidate.getId(), docType);
            
            if (doc.isEmpty()) {
                throw new WorkflowException(
                    "Cannot review documents: Missing required document - " + docType
                );
            }
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
