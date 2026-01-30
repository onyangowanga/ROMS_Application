package com.roms.service;


import com.roms.dto.CandidateWorkflowDTO;
import com.roms.entity.Candidate;
import com.roms.enums.CandidateStatus;
import com.roms.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import com.roms.service.DocumentEvaluationService;

@Service
public class CandidateWorkflowQueryService {
    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private DocumentEvaluationService documentEvaluationService;

    @Autowired
    private CandidateWorkflowService workflowService;


    public CandidateWorkflowDTO getWorkflowForApplicant(String email) {
        Candidate candidate = candidateRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("No active application found for applicant"));

        CandidateStatus status = candidate.getCurrentStatus();
        String blockReason = null;
        boolean blocked = false;
        List<String> missingDocs = Collections.emptyList();

        // Document evaluation for document-related stages
        if (status == CandidateStatus.UNDER_REVIEW || status == CandidateStatus.DOCUMENTS_INSUFFICIENT) {
            var eval = documentEvaluationService.evaluateDocuments(candidate);
            missingDocs = eval.missingDocuments;
            if (!eval.missingDocuments.isEmpty()) {
                blocked = true;
                blockReason = "Missing or invalid documents";
            }
        }

        // Payment/visa block
        if (status == CandidateStatus.VISA_PROCESSING) {
            String reason = workflowService.getTransitionBlockReason(candidate.getId(), CandidateStatus.VISA_PROCESSING);
            if (reason != null) {
                blocked = true;
                blockReason = reason;
            }
        }

        // General workflow block (guards)
        if (!blocked) {
            String reason = workflowService.getTransitionBlockReason(candidate.getId(), status);
            if (reason != null) {
                blocked = true;
                blockReason = reason;
            }
        }

        return new CandidateWorkflowDTO(
                status,
                getStageTitle(status),
                getStageDescription(status),
                blocked,
                blockReason,
                missingDocs
        );
    }

    private String getStageTitle(CandidateStatus status) {
        switch (status) {
            case APPLICATION_SUBMITTED: return "Application Submitted";
            case UNDER_REVIEW: return "Documents Under Review";
            case DOCUMENTS_INSUFFICIENT: return "Documents Insufficient";
            case DOCUMENTS_APPROVED: return "Documents Approved";
            case INTERVIEW_SCHEDULED: return "Interview Scheduled";
            case INTERVIEW_PASSED: return "Interview Passed";
            case MEDICAL_PENDING: return "Medical Pending";
            case MEDICAL_PASSED: return "Medical Passed";
            case VISA_PROCESSING: return "Visa Processing";
            case OFFER_ISSUED: return "Offer Issued";
            case OFFER_ACCEPTED: return "Offer Accepted";
            case DEPLOYMENT_PENDING: return "Deployment Pending";
            case PLACED: return "Placed";
            case REJECTED: return "Rejected";
            default: return status.name();
        }
    }

    private String getStageDescription(CandidateStatus status) {
        switch (status) {
            case APPLICATION_SUBMITTED: return "Your application has been submitted and is awaiting review.";
            case UNDER_REVIEW: return "Your documents are being reviewed by our staff.";
            case DOCUMENTS_INSUFFICIENT: return "Some required documents are missing or invalid. Please upload the requested documents.";
            case DOCUMENTS_APPROVED: return "All required documents have been approved. Await further instructions.";
            case INTERVIEW_SCHEDULED: return "Your interview has been scheduled. Please check the details and prepare accordingly.";
            case INTERVIEW_PASSED: return "You have passed the interview. Next steps will be communicated soon.";
            case MEDICAL_PENDING: return "Medical examination is pending. Please complete the required medical tests.";
            case MEDICAL_PASSED: return "You have passed the medical examination.";
            case VISA_PROCESSING: return "Visa processing is underway. Ensure all payments are complete.";
            case OFFER_ISSUED: return "A job offer has been issued. Please review and accept if you agree.";
            case OFFER_ACCEPTED: return "You have accepted the job offer. Deployment will be scheduled.";
            case DEPLOYMENT_PENDING: return "Deployment is pending. Await further instructions.";
            case PLACED: return "Congratulations! You have been placed successfully.";
            case REJECTED: return "Your application has been rejected.";
            default: return "Status: " + status.name();
        }
    }
}
