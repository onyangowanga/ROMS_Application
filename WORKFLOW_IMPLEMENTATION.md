# Comprehensive Workflow Implementation

## Overview
This document describes the complete 14-stage candidate workflow system with automatic document checking, progress tracking, and interview scheduling.

## Workflow Stages

### 1. Application Phase
- **APPLIED**: Initial application submitted
- **DOCUMENTS_PENDING**: Missing required documents (Passport, Photo, CV)
- **DOCUMENTS_UNDER_REVIEW**: All required documents uploaded and being reviewed

### 2. Document Review Phase
- **DOCUMENTS_APPROVED**: Documents verified and approved

### 3. Interview Phase
- **INTERVIEW_SCHEDULED**: Interview date/time/location set
- **INTERVIEW_COMPLETED**: Interview conducted

### 4. Medical Phase
- **MEDICAL_IN_PROGRESS**: Medical examination in progress
- **MEDICAL_PASSED**: Medical examination passed

### 5. Offer Phase
- **OFFER_ISSUED**: Job offer sent to candidate
- **OFFER_SIGNED**: Candidate accepted and signed offer

### 6. Deployment Phase
- **DEPLOYED**: Candidate deployed to position
- **PLACED**: Successfully placed and working

### 7. Terminal States
- **REJECTED**: Application rejected (can happen at any stage)
- **WITHDRAWN**: Candidate withdrew application

## Features Implemented

### 1. Automatic Document Checking
**File**: `JobApplicationService.java`

When a candidate applies:
- System automatically checks for required documents (Passport, Photo, CV)
- If all present → Status set to `DOCUMENTS_UNDER_REVIEW`
- If any missing → Status set to `DOCUMENTS_PENDING`

```java
private CandidateStatus determineInitialStatus(Candidate candidate) {
    boolean hasPassport = documentRepository
        .findByCandidateIdAndDocType(candidate.getId(), DocumentType.PASSPORT)
        .isPresent();
    // ... checks for other documents
    
    if (hasPassport && hasPhoto && hasCV) {
        return CandidateStatus.DOCUMENTS_UNDER_REVIEW;
    }
    return CandidateStatus.DOCUMENTS_PENDING;
}
```

### 2. Workflow Guard Logic
**File**: `CandidateWorkflowService.java`

Enhanced guard logic prevents invalid transitions:

#### Document Validation
```java
case DOCUMENTS_UNDER_REVIEW:
    validateRequiredDocuments(candidate);
    break;
```

#### Interview Requirements
```java
case INTERVIEW_SCHEDULED:
    if (candidate.getInterviewDate() == null) {
        throw new WorkflowException("Interview date is required");
    }
    break;
```

#### Medical Requirements
```java
case OFFER_ISSUED:
    if (candidate.getMedicalStatus() != MedicalStatus.PASSED) {
        throw new WorkflowException("Medical must be passed before offer");
    }
    break;
```

### 3. Progress Tracking
**File**: `MyApplicationPage.tsx`

Visual progress bar shows completion percentage:

```typescript
const getProgressPercentage = (status: CandidateStatus): number => {
  const progressMap: Record<CandidateStatus, number> = {
    APPLIED: 7,
    DOCUMENTS_PENDING: 14,
    DOCUMENTS_UNDER_REVIEW: 21,
    DOCUMENTS_APPROVED: 28,
    INTERVIEW_SCHEDULED: 42,
    INTERVIEW_COMPLETED: 50,
    MEDICAL_IN_PROGRESS: 64,
    MEDICAL_PASSED: 71,
    OFFER_ISSUED: 85,
    OFFER_SIGNED: 92,
    DEPLOYED: 96,
    PLACED: 100,
    REJECTED: 0,
    WITHDRAWN: 0,
  };
  return progressMap[status] || 0;
};
```

**Features**:
- Color-coded progress bar (Blue: in progress, Green: placed, Red: rejected)
- Percentage display (7-100%)
- Current stage label (e.g., "Documents Under Review")

### 4. Interview Scheduling
**File**: `CandidateProfilePage.tsx`

Staff can schedule interviews with:
- **Interview Date**: Date picker
- **Interview Time**: Time picker
- **Interview Location**: Text field (office, address, or virtual meeting)
- **Interview Notes**: Additional details or meeting links

**Visibility**:
- Scheduling form: Visible when status is `DOCUMENTS_APPROVED` or later
- Interview details: Automatically displayed to applicant in their dashboard

**Backend Support**:
- New fields in `Candidate.java`: `interviewDate`, `interviewTime`, `interviewLocation`, `interviewNotes`
- Updated `CandidateController.java` PUT endpoint to support partial updates

### 5. Applicant Dashboard
**File**: `MyApplicationPage.tsx`

Applicants can:
- View all their applications
- See progress bar for each application
- Upload documents within each application card
- View interview details when scheduled
- Apply for new jobs

**Interview Display**:
```tsx
{app.interviewDate && (
  <div className="mt-2 p-2 bg-blue-50 border border-blue-200 rounded">
    <div className="flex items-center">
      <CalendarIcon />
      <span>Interview: {new Date(app.interviewDate).toLocaleDateString()}</span>
      {app.interviewTime && <span>at {app.interviewTime}</span>}
    </div>
    {app.interviewLocation && (
      <div className="flex items-center">
        <LocationIcon />
        <span>{app.interviewLocation}</span>
      </div>
    )}
  </div>
)}
```

## Workflow Transitions

### Valid Transition Paths

```
APPLIED
  ├─→ DOCUMENTS_PENDING
  ├─→ DOCUMENTS_UNDER_REVIEW
  ├─→ REJECTED
  └─→ WITHDRAWN

DOCUMENTS_PENDING
  ├─→ DOCUMENTS_UNDER_REVIEW (when documents uploaded)
  ├─→ REJECTED
  └─→ WITHDRAWN

DOCUMENTS_UNDER_REVIEW
  ├─→ DOCUMENTS_APPROVED (staff approval)
  ├─→ DOCUMENTS_PENDING (needs more docs)
  ├─→ REJECTED
  └─→ WITHDRAWN

DOCUMENTS_APPROVED
  ├─→ INTERVIEW_SCHEDULED
  ├─→ MEDICAL_IN_PROGRESS (skip interview)
  ├─→ REJECTED
  └─→ WITHDRAWN

INTERVIEW_SCHEDULED
  ├─→ INTERVIEW_COMPLETED
  ├─→ REJECTED
  └─→ WITHDRAWN

INTERVIEW_COMPLETED
  ├─→ MEDICAL_IN_PROGRESS
  ├─→ REJECTED
  └─→ WITHDRAWN

MEDICAL_IN_PROGRESS
  ├─→ MEDICAL_PASSED
  ├─→ REJECTED
  └─→ WITHDRAWN

MEDICAL_PASSED
  ├─→ OFFER_ISSUED
  ├─→ REJECTED
  └─→ WITHDRAWN

OFFER_ISSUED
  ├─→ OFFER_SIGNED
  ├─→ REJECTED
  └─→ WITHDRAWN

OFFER_SIGNED
  ├─→ DEPLOYED
  └─→ WITHDRAWN

DEPLOYED
  ├─→ PLACED
  └─→ WITHDRAWN

PLACED (Terminal - Success)
REJECTED (Terminal - Failed)
WITHDRAWN (Terminal - Cancelled)
```

## Guard Logic Rules

### 1. Document Rules
- **DOCUMENTS_UNDER_REVIEW**: Requires Passport, Photo, and CV to be uploaded
- **DOCUMENTS_APPROVED**: Validates passport expiry (minimum 6 months validity)

### 2. Interview Rules
- **INTERVIEW_SCHEDULED**: Requires `interviewDate` to be set

### 3. Medical Rules
- **MEDICAL_IN_PROGRESS**: Sets medical status to PENDING
- **MEDICAL_PASSED**: Sets medical status to PASSED
- **OFFER_ISSUED**: Requires medical status to be PASSED

### 4. Placement Rules
- **PLACED**: Validates job order capacity and increments filled count

## API Endpoints

### Workflow Transition
```http
POST /api/candidates/{id}/transition
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "DOCUMENTS_APPROVED"
}
```

### Update Interview Details
```http
PUT /api/candidates/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "interviewDate": "2026-02-15",
  "interviewTime": "10:00 AM",
  "interviewLocation": "Conference Room A",
  "interviewNotes": "Please bring original documents"
}
```

### Get Applicant's Applications
```http
GET /api/candidates/me?email={email}
Authorization: Bearer {token}
```

## Database Changes

### Candidate Table (New Fields)
```sql
ALTER TABLE candidates 
  ADD COLUMN interview_date DATE,
  ADD COLUMN interview_time VARCHAR(50),
  ADD COLUMN interview_location VARCHAR(255),
  ADD COLUMN interview_notes TEXT;
```

### Updated Status Enum
```sql
-- CandidateStatus enum now includes:
-- APPLIED, DOCUMENTS_PENDING, DOCUMENTS_UNDER_REVIEW, DOCUMENTS_APPROVED,
-- INTERVIEW_SCHEDULED, INTERVIEW_COMPLETED, MEDICAL_IN_PROGRESS, MEDICAL_PASSED,
-- OFFER_ISSUED, OFFER_SIGNED, DEPLOYED, PLACED, REJECTED, WITHDRAWN
```

## User Experience

### For Applicants
1. **Apply for Job**: System auto-checks documents
2. **Upload Documents**: Can upload at any time
3. **Track Progress**: Visual progress bar shows completion %
4. **View Interview**: Date/time/location displayed when scheduled
5. **Monitor Status**: Clear status labels (e.g., "Documents Approved")

### For Staff
1. **Review Documents**: Transition to DOCUMENTS_APPROVED when verified
2. **Schedule Interview**: Set date/time/location (visible to applicant)
3. **Record Interview**: Mark as INTERVIEW_COMPLETED
4. **Process Medical**: Transition through medical stages
5. **Issue Offer**: System enforces medical clearance requirement
6. **Deploy & Place**: Final placement with capacity validation

## Benefits

### 1. Automation
- Auto-detects missing documents
- Validates transitions automatically
- Prevents invalid workflow states

### 2. Transparency
- Applicants see exactly where they are
- Progress percentage shows completion
- Interview details visible immediately

### 3. Compliance
- Passport validity enforced
- Medical clearance required before offer
- Job capacity validated before placement

### 4. Flexibility
- Can skip interview if not needed (DOCUMENTS_APPROVED → MEDICAL_IN_PROGRESS)
- Can return to DOCUMENTS_PENDING if more documents needed
- Can reject or withdraw at any stage

## Future Enhancements

### 1. Notifications
- Email notifications at each stage transition
- SMS alerts for interview scheduling
- Deadline reminders for document submission

### 2. Offer Letter Management
- Upload offer letter document
- Digital signature capture
- Automatic transition to OFFER_SIGNED when signed

### 3. Document Expiry Tracking
- Automated expiry date monitoring
- Renewal reminders
- Automatic status updates when documents expire

### 4. Interview Calendar Integration
- Calendar invites sent to applicants
- Outlook/Google Calendar sync
- Automated interview reminders

### 5. Analytics Dashboard
- Average time per workflow stage
- Bottleneck identification
- Success/rejection rate tracking

## Testing Scenarios

### Scenario 1: Complete Application Flow
1. Applicant applies with all documents → Status: DOCUMENTS_UNDER_REVIEW (21%)
2. Staff approves documents → Status: DOCUMENTS_APPROVED (28%)
3. Staff schedules interview → Status: INTERVIEW_SCHEDULED (42%)
4. Interview completed → Status: INTERVIEW_COMPLETED (50%)
5. Medical starts → Status: MEDICAL_IN_PROGRESS (64%)
6. Medical passes → Status: MEDICAL_PASSED (71%)
7. Offer issued → Status: OFFER_ISSUED (85%)
8. Offer signed → Status: OFFER_SIGNED (92%)
9. Deployed → Status: DEPLOYED (96%)
10. Placed → Status: PLACED (100%)

### Scenario 2: Missing Documents Flow
1. Applicant applies without CV → Status: DOCUMENTS_PENDING (14%)
2. Applicant uploads CV → Staff can transition to DOCUMENTS_UNDER_REVIEW (21%)
3. Continue normal flow...

### Scenario 3: Skip Interview Flow
1. Documents approved → Status: DOCUMENTS_APPROVED (28%)
2. Directly to medical → Status: MEDICAL_IN_PROGRESS (64%)
3. Continue to placement...

## Summary

This comprehensive workflow system provides:
- ✅ **14-stage workflow** with clear progression
- ✅ **Automatic document checking** on application
- ✅ **Visual progress tracking** for applicants (7-100%)
- ✅ **Interview scheduling** with full details
- ✅ **Guard logic validation** to enforce rules
- ✅ **Flexible transitions** with optional interview
- ✅ **Multi-application support** for applicants
- ✅ **Role-based access control** for staff operations

The system is production-ready and provides a complete recruitment workflow from application to placement.
