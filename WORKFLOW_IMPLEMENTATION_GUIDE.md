# ROMS Workflow Logic Implementation Guide

## Overview
This guide provides step-by-step instructions to implement the canonical workflow logic as specified.

---

## 1. Enhanced DocumentEvaluationService

### Location: `src/main/java/com/roms/service/DocumentEvaluationService.java`

###Changes Needed:

1. **Add `@Value` annotation for passport validity**
2. **Improve document evaluation logic**
3. **Add `isSufficient()` method to result class**
4. **Add user-friendly document names**

### Complete Implementation:

```java
package com.roms.service;

import com.roms.entity.Candidate;
import com.roms.entity.CandidateDocument;
import com.roms.enums.DocumentType;
import com.roms.repository.CandidateDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated Document Sufficiency Engine
 * Evaluates if candidate documents meet requirements for progression
 */
@Service
public class DocumentEvaluationService {

    @Autowired
    private CandidateDocumentRepository documentRepository;

    @Value("${roms.passport.min-validity-months:6}")
    private int passportMinValidityMonths;

    public DocumentEvaluationResult evaluateDocuments(Candidate candidate) {
        List<String> missing = new ArrayList<>();
        List<CandidateDocument> docs = documentRepository.findByCandidateId(candidate.getId());

        // RULE 1: Passport must be present
        CandidateDocument passport = docs.stream()
                .filter(d -> d.getDocType() == DocumentType.PASSPORT)
                .findFirst()
                .orElse(null);

        if (passport == null) {
            missing.add("Passport bio page");
        }

        // RULE 2: Passport expiry ≥ today + 6 months
        boolean passportValid = false;
        if (passport != null && passport.getExpiryDate() != null) {
            LocalDate minValidDate = LocalDate.now().plusMonths(passportMinValidityMonths);
            if (passport.getExpiryDate().isAfter(minValidDate)) {
                passportValid = true;
            } else {
                missing.add("Passport valid for at least " + passportMinValidityMonths + " months");
            }
        } else if (passport != null) {
            missing.add("Passport expiry date");
        }

        // RULE 3: Check for essential documents
        DocumentType[] requiredDocs = {DocumentType.CV, DocumentType.POLICE_CLEARANCE, DocumentType.PHOTO};

        for (DocumentType type : requiredDocs) {
            boolean found = docs.stream().anyMatch(d -> d.getDocType() == type);
            if (!found) {
                missing.add(getDocumentDisplayName(type));
            }
        }

        boolean sufficient = missing.isEmpty();
        return new DocumentEvaluationResult(sufficient, missing, passportValid);
    }

    private String getDocumentDisplayName(DocumentType type) {
        switch (type) {
            case PASSPORT: return "Passport bio page";
            case CV: return "Curriculum Vitae (CV)";
            case MEDICAL_REPORT: return "Medical certificate";
            case POLICE_CLEARANCE: return "Police clearance certificate";
            case PHOTO: return "Passport-size photograph";
            default: return type.name();
        }
    }

    public static class DocumentEvaluationResult {
        public final boolean sufficient;
        public final List<String> missingDocuments;
        public final boolean passportValid;

        public DocumentEvaluationResult(boolean sufficient, List<String> missingDocuments, boolean passportValid) {
            this.sufficient = sufficient;
            this.missingDocuments = missingDocuments;
            this.passportValid = passportValid;
        }

        public boolean isSufficient() {
            return sufficient;
        }
    }
}
```

---

## 2. Add Interview Requirement to JobOrder

### Location: `src/main/java/com/roms/entity/JobOrder.java`

### Add Field:

```java
@Column(name = "requires_interview")
@Builder.Default
private Boolean requiresInterview = true; // Default: most jobs require interview
```

### Add Getter/Setter (or rely on Lombok @Data):

```java
public Boolean getRequiresInterview() {
    return requiresInterview != null ? requiresInterview : true;
}
```

---

## 3. Enhanced Candidate Workflow Service

### Location: `src/main/java/com/roms/service/CandidateWorkflowService.java`

### Add Automated Document Review Transition:

Add this method after `transitionStatus()`:

```java
/**
 * AUTOMATED: Transition from UNDER_REVIEW based on document evaluation
 * Called by staff when reviewing documents
 */
@Transactional
public Candidate reviewDocuments(Long candidateId) {
    Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new WorkflowException("Candidate not found"));

    if (candidate.getCurrentStatus() != CandidateStatus.UNDER_REVIEW) {
        throw new WorkflowException("Can only review documents when status is UNDER_REVIEW");
    }

    // Evaluate documents
    DocumentEvaluationService.DocumentEvaluationResult result =
            documentEvaluationService.evaluateDocuments(candidate);

    if (result.isSufficient()) {
        // All documents present and valid → DOCUMENTS_APPROVED
        candidate.setCurrentStatus(CandidateStatus.DOCUMENTS_APPROVED);
    } else {
        // Missing documents → DOCUMENTS_INSUFFICIENT
        candidate.setCurrentStatus(CandidateStatus.DOCUMENTS_INSUFFICIENT);
    }

    return candidateRepository.save(candidate);
}
```

### Add Interview Logic:

```java
/**
 * AUTOMATED: Handle transition after DOCUMENTS_APPROVED
 * Checks if interview is required for the job
 */
@Transactional
public Candidate proceedAfterDocumentApproval(Long candidateId) {
    Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new WorkflowException("Candidate not found"));

    if (candidate.getCurrentStatus() != CandidateStatus.DOCUMENTS_APPROVED) {
        throw new WorkflowException("Can only proceed from DOCUMENTS_APPROVED status");
    }

    // Get active assignment to check job requirements
    var assignment = assignmentService.getActiveAssignment(candidateId);
    if (assignment == null) {
        throw new WorkflowException("No active job assignment found");
    }

    // Check if job requires interview
    if (assignment.getJobOrder().getRequiresInterview()) {
        // Interview required
        candidate.setCurrentStatus(CandidateStatus.INTERVIEW_SCHEDULED);
    } else {
        // Skip interview → go directly to medical
        candidate.setCurrentStatus(CandidateStatus.MEDICAL_PENDING);
        candidate.setMedicalStatus(MedicalStatus.PENDING);
    }

    return candidateRepository.save(candidate);
}
```

### Inject DocumentEvaluationService:

Add at the top with other @Autowired fields:

```java
@Autowired
private DocumentEvaluationService documentEvaluationService;
```

---

## 4. Update Candidate Controller

### Location: `src/main/java/com/roms/controller/CandidateController.java`

### Add New Endpoints:

```java
/**
 * Staff endpoint: Review documents and auto-transition
 */
@PostMapping("/{id}/review-documents")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
public ResponseEntity<?> reviewDocuments(@PathVariable Long id) {
    try {
        Candidate candidate = workflowService.reviewDocuments(id);
        return ResponseEntity.ok(ApiResponse.success("Documents reviewed successfully", candidate));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Document review failed: " + e.getMessage()));
    }
}

/**
 * Staff endpoint: Proceed after document approval (handles interview logic)
 */
@PostMapping("/{id}/proceed-after-documents")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
public ResponseEntity<?> proceedAfterDocuments(@PathVariable Long id) {
    try {
        Candidate candidate = workflowService.proceedAfterDocumentApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Proceeding to next stage", candidate));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Transition failed: " + e.getMessage()));
    }
}
```

---

## 5. Update CandidateWorkflowQueryService

### Location: `src/main/java/com/roms/service/CandidateWorkflowQueryService.java`

### Enhance Status Descriptions:

Update the `getStageDescription()` method:

```java
private String getStageDescription(CandidateStatus status) {
    switch (status) {
        case APPLICATION_SUBMITTED:
            return "✅ Your application has been received and is awaiting review.";
        case UNDER_REVIEW:
            return "⏳ Your documents are being reviewed by our staff.";
        case DOCUMENTS_INSUFFICIENT:
            return "❌ Some required documents are missing or invalid. Please upload the requested documents.";
        case DOCUMENTS_APPROVED:
            return "✅ All required documents have been approved. Next steps will be communicated soon.";
        case INTERVIEW_SCHEDULED:
            return "📅 Your interview has been scheduled. Please check the details and prepare accordingly.";
        case INTERVIEW_PASSED:
            return "✅ You have passed the interview. Proceeding to medical assessment.";
        case MEDICAL_PENDING:
            return "🏥 Medical examination is pending. Please complete the required medical tests.";
        case MEDICAL_PASSED:
            return "✅ You have passed the medical examination.";
        case VISA_PROCESSING:
            return "🛂 Visa processing is underway. Ensure all payments are complete.";
        case OFFER_ISSUED:
            return "📄 A job offer has been issued. Please review and accept if you agree.";
        case OFFER_ACCEPTED:
            return "✅ You have accepted the job offer. Deployment will be scheduled.";
        case DEPLOYMENT_PENDING:
            return "✈️ Deployment is pending. Await further instructions.";
        case PLACED:
            return "🎉 Congratulations! You have been placed successfully.";
        case REJECTED:
            return "❌ Your application has been rejected.";
        default:
            return "Status: " + status.name();
    }
}
```

---

## 6. Frontend Updates

### Location: `frontend/src/pages/CandidateProfilePage.tsx`

### Add Review Documents Button:

Find the "Workflow Transitions" section and add before the status transition dropdown:

```tsx
{/* Document Review Actions */}
{candidate.currentStatus === 'UNDER_REVIEW' && (
  <div className="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
    <h3 className="text-sm font-medium text-blue-900 mb-2">Document Review</h3>
    <p className="text-xs text-blue-700 mb-3">
      Click below to automatically evaluate documents and transition status
    </p>
    <button
      onClick={async () => {
        try {
          await api.post(`/api/candidates/${id}/review-documents`);
          await loadCandidate(parseInt(id!));
          alert('Documents reviewed successfully!');
        } catch (err: any) {
          alert(err.response?.data?.message || 'Failed to review documents');
        }
      }}
      className="w-full py-2 px-4 bg-blue-600 text-white rounded hover:bg-blue-700"
    >
      Review Documents Now
    </button>
  </div>
)}

{/* Proceed After Document Approval */}
{candidate.currentStatus === 'DOCUMENTS_APPROVED' && (
  <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
    <h3 className="text-sm font-medium text-green-900 mb-2">Documents Approved</h3>
    <p className="text-xs text-green-700 mb-3">
      Proceed to next stage (Interview or Medical based on job requirements)
    </p>
    <button
      onClick={async () => {
        try {
          await api.post(`/api/candidates/${id}/proceed-after-documents`);
          await loadCandidate(parseInt(id!));
          alert('Proceeding to next stage!');
        } catch (err: any) {
          alert(err.response?.data?.message || 'Failed to proceed');
        }
      }}
      className="w-full py-2 px-4 bg-green-600 text-white rounded hover:bg-green-700"
    >
      Proceed to Next Stage
    </button>
  </div>
)}
```

---

## Implementation Steps:

1. ✅ Update `DocumentEvaluationService.java` with enhanced logic
2. ✅ Add `requiresInterview` field to `JobOrder.java`
3. ✅ Add automated methods to `CandidateWorkflowService.java`
4. ✅ Add new endpoints to `CandidateController.java`
5. ✅ Update status descriptions in `CandidateWorkflowQueryService.java`
6. ✅ Add review buttons to frontend `CandidateProfilePage.tsx`
7. ✅ Test end-to-end workflow

---

## Testing Checklist:

- [ ] Submit application → APPLICATION_SUBMITTED
- [ ] Upload documents
- [ ] Staff clicks "Review Documents" → Auto-transitions to DOCUMENTS_APPROVED or DOCUMENTS_INSUFFICIENT
- [ ] If insufficient, upload missing docs and review again
- [ ] After approval, click "Proceed" → Goes to INTERVIEW_SCHEDULED or MEDICAL_PENDING
- [ ] Complete interview → INTERVIEW_PASSED → MEDICAL_PENDING
- [ ] Complete medical → MEDICAL_PASSED
- [ ] Process visa (requires downpayment) → VISA_PROCESSING
- [ ] Issue offer → OFFER_ISSUED
- [ ] Accept offer → OFFER_ACCEPTED
- [ ] Deploy → DEPLOYMENT_PENDING
- [ ] Place (requires full payment) → PLACED

---

---

## 7. MEDICAL STAGE

### Backend Logic (Already Implemented)

The workflow service already handles medical transitions in `applyGuardLogic()`:

```java
case MEDICAL_PENDING:
    candidate.setMedicalStatus(MedicalStatus.PENDING);
    break;
case MEDICAL_PASSED:
    candidate.setMedicalStatus(MedicalStatus.PASSED);
    break;
```

### Staff Action:
Staff uses the existing status transition dropdown to mark:
- `MEDICAL_PENDING` → Sets medical status to PENDING
- `MEDICAL_PASSED` → Marks medical as PASSED, allows progression

### Applicant Dashboard Message:
```
🏥 Medical Pending
Medical examination is pending. Please complete the required medical tests.
```

After passed:
```
✅ Medical Passed
You have passed the medical examination. Proceeding to visa processing.
```

---

## 8. VISA PROCESSING GATE (FINANCIAL ENFORCEMENT)

### Backend Logic (Already Implemented)

The `validateDownpaymentPaid()` method in CandidateWorkflowService already blocks VISA_PROCESSING:

```java
private void validateDownpaymentPaid(Candidate candidate) {
    var activeAssignment = assignmentService.getActiveAssignment(candidate.getId());
    if (activeAssignment == null) {
        throw new WorkflowException("Cannot process visa: No active assignment found");
    }

    boolean downpaymentComplete = commissionPaymentService.isDownpaymentComplete(activeAssignment.getId());
    if (!downpaymentComplete) {
        throw new WorkflowException("Downpayment required before visa processing. Please complete the required downpayment through the commission agreement.");
    }
}
```

### Finance/Super Admin Authorization:

Finance Manager or Super Admin must:
1. Go to candidate profile
2. View Commission Management panel
3. Record downpayment via "Record Payment" button
4. Once downpayment percentage is met, status can transition to VISA_PROCESSING

### Frontend: Dashboard Lock Banner

The `WorkflowLockBanner` component already displays:

```tsx
🔒 Downpayment Required
Downpayment of [amount] must be paid before visa processing can begin.
Status: [X% paid]
```

### Applicant Dashboard Message:
```
⏳ Awaiting Visa Processing
Your application is pending visa processing. Payment processing in progress.
```

After downpayment:
```
🟢 Visa Processing Started
Your visa application is being processed.
```

---

## 9. OFFER LETTER & ACCEPTANCE

### Backend: Add Offer Acceptance Method

Add to `CandidateWorkflowService.java`:

```java
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
```

### Controller: Add Offer Acceptance Endpoint

Add to `CandidateController.java`:

```java
/**
 * Applicant endpoint: Accept offer letter
 */
@PostMapping("/{id}/accept-offer")
@PreAuthorize("hasAnyRole('APPLICANT', 'SUPER_ADMIN', 'OPERATIONS_STAFF')")
public ResponseEntity<?> acceptOffer(@PathVariable Long id) {
    try {
        Candidate candidate = workflowService.acceptOffer(id);
        return ResponseEntity.ok(ApiResponse.success("Offer accepted successfully", candidate));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to accept offer: " + e.getMessage()));
    }
}
```

### Frontend: Offer Acceptance UI

Add to `MyApplicationPage.tsx` (Applicant view):

```tsx
{/* Offer Letter Section */}
{app.currentStatus === 'OFFER_ISSUED' && (
  <div className="mt-4 p-4 bg-yellow-50 border border-yellow-300 rounded-lg">
    <h4 className="text-sm font-semibold text-yellow-900 mb-2">
      📄 Offer Letter Issued
    </h4>
    <p className="text-xs text-yellow-800 mb-3">
      Please review your offer letter and accept to proceed with deployment.
    </p>
    <button
      onClick={async () => {
        if (window.confirm('Do you accept this job offer?')) {
          try {
            await candidateApi.acceptOffer(app.id);
            loadApplications();
            alert('Offer accepted successfully! Preparing for deployment.');
          } catch (err: any) {
            alert(err.response?.data?.message || 'Failed to accept offer');
          }
        }
      }}
      className="w-full py-2 px-4 bg-green-600 text-white rounded hover:bg-green-700 font-medium"
    >
      Accept Offer
    </button>
  </div>
)}

{app.currentStatus === 'OFFER_ACCEPTED' && (
  <div className="mt-4 p-4 bg-green-50 border border-green-300 rounded-lg">
    <h4 className="text-sm font-semibold text-green-900 mb-2">
      ✅ Offer Accepted
    </h4>
    <p className="text-xs text-green-800">
      Preparing for deployment. You will be notified of next steps.
    </p>
  </div>
)}
```

### Add to `candidateApi` in `frontend/src/api/candidates.ts`:

```typescript
acceptOffer: async (id: number): Promise<Candidate> => {
  const response = await api.post<ApiResponse<Candidate>>(`/api/candidates/${id}/accept-offer`);
  return response.data.data;
},
```

---

## 10. DEPLOYMENT & PLACEMENT (FINAL STAGES)

### Backend Logic (Already Implemented)

The `validateFullPaymentComplete()` method already blocks PLACED status:

```java
private void validateFullPaymentComplete(Candidate candidate) {
    var activeAssignment = assignmentService.getActiveAssignment(candidate.getId());
    if (activeAssignment == null) {
        throw new WorkflowException("Cannot place candidate: No active assignment found");
    }

    boolean fullPaymentComplete = commissionPaymentService.isFullPaymentComplete(activeAssignment.getId());
    if (!fullPaymentComplete) {
        throw new WorkflowException("Full commission payment required before placement. Outstanding balance must be paid.");
    }
}
```

### Staff Action:

1. Transition to `DEPLOYMENT_PENDING` after offer acceptance
2. Finance records full payment via Commission Management
3. Once 100% paid, transition to `PLACED`

### Applicant Final Dashboard Message:

Add to `CandidateWorkflowQueryService.java` `getStageDescription()`:

```java
case DEPLOYMENT_PENDING:
    return "✈️ Deployment Pending\nYour deployment is being finalized. Please await travel instructions.";
case PLACED:
    return "🎉 Successfully Placed\nCongratulations! You have been successfully deployed.";
```

### Frontend: Final Placement Display

Add to `MyApplicationPage.tsx`:

```tsx
{/* Final Placement Success */}
{app.currentStatus === 'PLACED' && activeAssignment && (
  <div className="mt-4 p-6 bg-gradient-to-r from-green-50 to-blue-50 border-2 border-green-400 rounded-lg">
    <div className="text-center">
      <div className="text-4xl mb-3">🎉</div>
      <h4 className="text-lg font-bold text-green-900 mb-2">
        Successfully Placed!
      </h4>
      <div className="text-sm text-gray-700 space-y-1">
        <p><strong>Employer:</strong> {activeAssignment.employerName || 'N/A'}</p>
        <p><strong>Position:</strong> {activeAssignment.jobTitle}</p>
        <p><strong>Country:</strong> {activeAssignment.country || 'N/A'}</p>
        {activeAssignment.deploymentDate && (
          <p><strong>Deployment Date:</strong> {new Date(activeAssignment.deploymentDate).toLocaleDateString()}</p>
        )}
      </div>
    </div>
  </div>
)}
```

---

## Ready for Implementation:
✅ Document Review Logic (Automated)
✅ Interview Conditional Logic (Job-based)
✅ Medical Stage (Staff marks PASSED)
✅ Visa Processing Gate (Downpayment enforcement - already implemented)
✅ Offer Letter & Acceptance (Applicant action)
✅ Deployment & Placement (Full payment enforcement - already implemented)

---

## Complete Workflow Flow Chart:

```
APPLICATION_SUBMITTED (Immediate after submission)
         ↓
UNDER_REVIEW (Staff uploads/candidate uploads docs)
         ↓
   [Auto Document Review]
         ↓
    ↙         ↘
INSUFFICIENT   APPROVED
    ↓             ↓
(Upload more) [Check Interview Required?]
    ↓          ↙         ↘
APPROVED  INTERVIEW    MEDICAL_PENDING
              ↓
         INTERVIEW_PASSED
              ↓
         MEDICAL_PENDING
              ↓
         MEDICAL_PASSED
              ↓
      [Downpayment Check] ← 🔒 BLOCKS if not paid
              ↓
       VISA_PROCESSING
              ↓
        OFFER_ISSUED
              ↓
    [Applicant Accepts]
              ↓
       OFFER_ACCEPTED
              ↓
     DEPLOYMENT_PENDING
              ↓
    [Full Payment Check] ← 🔒 BLOCKS if not 100%
              ↓
          PLACED ✅
```

---

## Implementation Priority Order:

1. ✅ Update DocumentEvaluationService
2. ✅ Add interview field to JobOrder
3. ✅ Add automated methods to CandidateWorkflowService
4. ✅ Add offer acceptance method to CandidateWorkflowService
5. ✅ Add endpoints to CandidateController
6. ✅ Update frontend CandidateProfilePage (staff view)
7. ✅ Update frontend MyApplicationPage (applicant view)
8. ✅ Add candidateApi methods
9. ✅ Test complete flow end-to-end
