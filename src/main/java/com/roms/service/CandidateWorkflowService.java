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
import com.roms.repository.JobOrderRepository;
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
 * Phase 2B Updates:
 * - VISA_PROCESSING requires downpayment
 * - PLACED requires full commission payment
 * 
 * Canonical State Flow:
 * APPLICATION_SUBMITTED → UNDER_REVIEW → DOCUMENTS_INSUFFICIENT → DOCUMENTS_APPROVED → INTERVIEW_SCHEDULED → INTERVIEW_PASSED → MEDICAL_PENDING → MEDICAL_PASSED → VISA_PROCESSING → OFFER_ISSUED → OFFER_ACCEPTED → DEPLOYMENT_PENDING → PLACED → REJECTED
 * OFFER_ACCEPTED → VISA_PROCESSING → VISA_APPROVED → DEPLOYED → PLACED
 */
@Service
public class CandidateWorkflowService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateDocumentRepository documentRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CommissionPaymentService commissionPaymentService;

    @Autowired
    private DocumentEvaluationService documentEvaluationService;

    @Autowired
    private JobOrderRepository jobOrderRepository;

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
     * AUTOMATED: Transition from UNDER_REVIEW based on document evaluation
     */
    @Transactional
    public Candidate reviewDocuments(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new WorkflowException("Candidate not found"));

        if (candidate.getCurrentStatus() != CandidateStatus.UNDER_REVIEW) {
            throw new WorkflowException("Can only review documents when status is UNDER_REVIEW");
        }

        DocumentEvaluationService.DocumentEvaluationResult result =
                documentEvaluationService.evaluateDocuments(candidate);

        if (result.isSufficient()) {
            candidate.setCurrentStatus(CandidateStatus.DOCUMENTS_APPROVED);
        } else {
            candidate.setCurrentStatus(CandidateStatus.DOCUMENTS_INSUFFICIENT);
        }

        return candidateRepository.save(candidate);
    }

    /**
     * AUTOMATED: Handle transition after DOCUMENTS_APPROVED
     */
    @Transactional
    public Candidate proceedAfterDocumentApproval(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new WorkflowException("Candidate not found"));

        if (candidate.getCurrentStatus() != CandidateStatus.DOCUMENTS_APPROVED) {
            throw new WorkflowException("Can only proceed from DOCUMENTS_APPROVED status");
        }

        var assignment = assignmentService.getActiveAssignment(candidateId);
        if (assignment == null) {
            throw new WorkflowException("No active job assignment found");
        }

        // Fetch the JobOrder to check if interview is required
        JobOrder jobOrder = jobOrderRepository.findById(assignment.getJobOrderId())
                .orElseThrow(() -> new WorkflowException("Job order not found"));

        Boolean requiresInterview = jobOrder.getRequiresInterview();
        if (requiresInterview == null || requiresInterview) {
            candidate.setCurrentStatus(CandidateStatus.INTERVIEW_SCHEDULED);
        } else {
            candidate.setCurrentStatus(CandidateStatus.MEDICAL_PENDING);
            candidate.setMedicalStatus(MedicalStatus.PENDING);
        }

        return candidateRepository.save(candidate);
    }

    /**
     * Applicant accepts offer letter
     */
    @Transactional
    public Candidate acceptOffer(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new WorkflowException("Candidate not found"));

        if (candidate.getCurrentStatus() != CandidateStatus.OFFER_ISSUED) {
            throw new WorkflowException("Can only accept offer when status is OFFER_ISSUED");
        }

        candidate.setCurrentStatus(CandidateStatus.OFFER_ACCEPTED);
        return candidateRepository.save(candidate);
    }

    /**
     * Validate if transition is allowed
     */
    private void validateTransition(Candidate candidate, CandidateStatus from, CandidateStatus to) {
        // Cannot transition if already in terminal states
        if (from == CandidateStatus.PLACED || from == CandidateStatus.REJECTED) {
            throw new WorkflowException("Cannot transition from terminal status: " + from);
        }

        // Define valid transitions for canonical workflow
        switch (to) {
            case UNDER_REVIEW:
                if (from != CandidateStatus.APPLICATION_SUBMITTED) {
                    throw new WorkflowException("Can only set UNDER_REVIEW from APPLICATION_SUBMITTED status");
                }
                break;
            case DOCUMENTS_INSUFFICIENT:
                if (from != CandidateStatus.UNDER_REVIEW) {
                    throw new WorkflowException("Can only set DOCUMENTS_INSUFFICIENT from UNDER_REVIEW status");
                }
                break;
            case DOCUMENTS_APPROVED:
                if (from != CandidateStatus.UNDER_REVIEW && from != CandidateStatus.DOCUMENTS_INSUFFICIENT) {
                    throw new WorkflowException("Can only approve documents from UNDER_REVIEW or DOCUMENTS_INSUFFICIENT status");
                }
                break;
            case INTERVIEW_SCHEDULED:
                if (from != CandidateStatus.DOCUMENTS_APPROVED) {
                    throw new WorkflowException("Can only schedule interview after documents are approved");
                }
                break;
            case INTERVIEW_PASSED:
                if (from != CandidateStatus.INTERVIEW_SCHEDULED) {
                    throw new WorkflowException("Can only pass interview after it is scheduled");
                }
                break;
            case MEDICAL_PENDING:
                if (from != CandidateStatus.INTERVIEW_PASSED) {
                    throw new WorkflowException("Can only set MEDICAL_PENDING after interview is passed");
                }
                break;
            case MEDICAL_PASSED:
                if (from != CandidateStatus.MEDICAL_PENDING) {
                    throw new WorkflowException("Can only pass medical from MEDICAL_PENDING status");
                }
                break;
            case VISA_PROCESSING:
                if (from != CandidateStatus.MEDICAL_PASSED) {
                    throw new WorkflowException("Can only start visa processing after medical is passed");
                }
                break;
            case OFFER_ISSUED:
                if (from != CandidateStatus.VISA_PROCESSING) {
                    throw new WorkflowException("Can only issue offer after visa processing");
                }
                break;
            case OFFER_ACCEPTED:
                if (from != CandidateStatus.OFFER_ISSUED) {
                    throw new WorkflowException("Can only accept offer after it is issued");
                }
                break;
            case DEPLOYMENT_PENDING:
                if (from != CandidateStatus.OFFER_ACCEPTED) {
                    throw new WorkflowException("Can only set DEPLOYMENT_PENDING after offer is accepted");
                }
                break;
            case PLACED:
                if (from != CandidateStatus.DEPLOYMENT_PENDING) {
                    throw new WorkflowException("Can only place candidate after deployment is pending");
                }
                break;
            case REJECTED:
                // Can reject from any non-terminal status
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
            case UNDER_REVIEW:
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
            case MEDICAL_PENDING:
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
            case VISA_PROCESSING:
                // CRITICAL PHASE 2B RULE: Downpayment required before visa processing
                validateDownpaymentPaid(candidate);
                break;
            case PLACED:
                // Fulfillment Rule: Check active assignment
                validateActiveAssignment(candidate);
                // CRITICAL PHASE 2B RULE: Full commission payment required before placement
                validateFullPaymentComplete(candidate);
                break;
            default:
                break;
        }
    }

    /**
     * Phase 2B: Validate downpayment is complete
     * BLOCKS VISA_PROCESSING transition
     */
    private void validateDownpaymentPaid(Candidate candidate) {
        // Get active assignment
        var activeAssignment = assignmentService.getActiveAssignment(candidate.getId());
        if (activeAssignment == null) {
            throw new WorkflowException("Cannot process visa: No active assignment found");
        }

        // Check downpayment status
        boolean downpaymentComplete = commissionPaymentService.isDownpaymentComplete(activeAssignment.getId());
        if (!downpaymentComplete) {
            throw new WorkflowException("Downpayment required before visa processing. Please complete the required downpayment through the commission agreement.");
        }
    }

    /**
     * Phase 2B: Validate full commission payment is complete
     * BLOCKS PLACED transition
     */
    private void validateFullPaymentComplete(Candidate candidate) {
        // Get active assignment
        var activeAssignment = assignmentService.getActiveAssignment(candidate.getId());
        if (activeAssignment == null) {
            throw new WorkflowException("Cannot place candidate: No active assignment found");
        }

        // Check full payment status
        boolean fullPaymentComplete = commissionPaymentService.isFullPaymentComplete(activeAssignment.getId());
        if (!fullPaymentComplete) {
            throw new WorkflowException("Full commission payment required before placement. Outstanding balance must be paid.");
        }
    }

    /**
     * Validate required documents are uploaded
     */
    private void validateRequiredDocuments(Candidate candidate) {
        // Check for essential documents
        DocumentType[] requiredDocs = {
            DocumentType.PASSPORT,
            DocumentType.CV,
            DocumentType.EDUCATIONAL_CERTIFICATE
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
     * Validate candidate has active assignment (Fulfillment Rule)
     */
    private void validateActiveAssignment(Candidate candidate) {
        if (!assignmentService.hasActiveAssignment(candidate.getId())) {
            throw new WorkflowException("Cannot place candidate: No active assignment found. Candidate must be assigned to a job order first.");
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
