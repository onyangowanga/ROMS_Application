# Remaining Implementation Steps

## ✅ COMPLETED:
1. DocumentEvaluationService - Enhanced with comprehensive rules

## 🔧 TO DO MANUALLY (Copy-Paste Code):

### 2. Add `requiresInterview` to JobOrder Entity

**File:** `src/main/java/com/roms/entity/JobOrder.java`

**Add this field** (after other fields):
```java
@Column(name = "requires_interview")
@Builder.Default
private Boolean requiresInterview = true;
```

---

### 3. Add Methods to CandidateWorkflowService

**File:** `src/main/java/com/roms/service/CandidateWorkflowService.java`

**Add @Autowired field** (after other @Autowired):
```java
@Autowired
private DocumentEvaluationService documentEvaluationService;
```

**Add these 3 methods** (after `transitionStatus()` method):

```java
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

    Boolean requiresInterview = assignment.getJobOrder().getRequiresInterview();
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
```

---

### 4. Add Endpoints to CandidateController

**File:** `src/main/java/com/roms/controller/CandidateController.java`

**Add these 3 endpoints** (before the closing brace `}`):

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
 * Staff endpoint: Proceed after document approval
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

---

### 5. Add acceptOffer to Frontend API

**File:** `frontend/src/api/candidates.ts`

**Add this method** (inside `export const candidateApi = {`, before closing `};`):

```typescript
acceptOffer: async (id: number): Promise<Candidate> => {
  const response = await api.post<ApiResponse<Candidate>>(`/api/candidates/${id}/accept-offer`);
  return response.data.data;
},
```

---

### 6. Update CandidateProfilePage (Staff View)

**File:** `frontend/src/pages/CandidateProfilePage.tsx`

**Find the "Workflow Transitions" section** (around line 705) and **add this BEFORE the status dropdown**:

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
          await candidateApi.reviewDocuments(parseInt(id!));
          await loadCandidate(parseInt(id!));
          await loadAllowedTransitions(parseInt(id!));
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
          await candidateApi.proceedAfterDocuments(parseInt(id!));
          await loadCandidate(parseInt(id!));
          await loadAllowedTransitions(parseInt(id!));
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

**Also add these helper methods to candidateApi** (in the `candidates.ts` file):

```typescript
reviewDocuments: async (id: number): Promise<Candidate> => {
  const response = await api.post<ApiResponse<Candidate>>(`/api/candidates/${id}/review-documents`);
  return response.data.data;
},

proceedAfterDocuments: async (id: number): Promise<Candidate> => {
  const response = await api.post<ApiResponse<Candidate>>(`/api/candidates/${id}/proceed-after-documents`);
  return response.data.data;
},
```

---

### 7. Update MyApplicationPage (Applicant View)

**File:** `frontend/src/pages/MyApplicationPage.tsx`

**Find where applications are displayed** (inside the map function where each application card is rendered) and **add this code AFTER the workflow timeline**:

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

{/* Final Placement Success */}
{app.currentStatus === 'PLACED' && (
  <div className="mt-4 p-6 bg-gradient-to-r from-green-50 to-blue-50 border-2 border-green-400 rounded-lg">
    <div className="text-center">
      <div className="text-4xl mb-3">🎉</div>
      <h4 className="text-lg font-bold text-green-900 mb-2">
        Successfully Placed!
      </h4>
      <div className="text-sm text-gray-700 space-y-1">
        <p><strong>Position:</strong> {app.expectedPosition || 'N/A'}</p>
        <p><strong>Reference:</strong> {app.internalRefNo}</p>
        <p className="text-xs text-gray-500 mt-2">
          Congratulations on your successful placement!
        </p>
      </div>
    </div>
  </div>
)}
```

---

## ⚡ QUICK IMPLEMENTATION CHECKLIST:

- [ ] 1. ✅ DocumentEvaluationService (DONE)
- [ ] 2. Add `requiresInterview` to JobOrder.java
- [ ] 3. Add 3 methods + @Autowired to CandidateWorkflowService.java
- [ ] 4. Add 3 endpoints to CandidateController.java
- [ ] 5. Add `acceptOffer` + helpers to frontend/src/api/candidates.ts
- [ ] 6. Add review buttons to CandidateProfilePage.tsx
- [ ] 7. Add offer acceptance UI to MyApplicationPage.tsx
- [ ] 8. Restart backend and frontend
- [ ] 9. Test complete workflow!

---

## 🧪 TESTING FLOW:

1. Submit application → APPLICATION_SUBMITTED
2. Staff clicks "Review Documents" → DOCUMENTS_APPROVED or DOCUMENTS_INSUFFICIENT
3. If insufficient, upload missing docs, review again
4. After approval, click "Proceed" → INTERVIEW_SCHEDULED or MEDICAL_PENDING
5. Complete workflow...interview → medical → visa → offer
6. Applicant accepts offer → OFFER_ACCEPTED
7. Staff marks deployment → PLACED

---

**All code is ready to copy-paste. Just follow the checklist above!**
