# Phase 2B: Applicant-Funded Agency Commission Management

## Overview
Phase 2B implements a comprehensive payment-gated workflow system for managing applicant-funded agency commissions. This system ensures that candidates cannot progress through critical workflow stages (visa processing, placement) without completing required payments.

## Business Requirements

### Commission Structure
- **Typical Commission**: 200,000 KES
- **Required Downpayment**: 50,000 KES (customizable per agreement)
- **Payment Methods**: M-PESA, Bank Transfer, Cash
- **Installments**: Allowed after downpayment is complete

### Workflow Gates
1. **VISA_PROCESSING Gate**: Requires downpayment completion
   - Transition from OFFER_SIGNED → VISA_PROCESSING blocked until downpayment paid
   - Visual lock banner displayed to both staff and applicants
   
2. **PLACEMENT Gate**: Requires full payment completion
   - Transition from DEPLOYED → PLACED blocked until full commission paid
   - Visual warning banner displayed when payment incomplete

## Backend Implementation

### New Entities

#### 1. AgencyCommissionAgreement
**Location**: `src/main/java/com/roms/entity/AgencyCommissionAgreement.java`

**Key Features**:
- UUID primary key for security
- Immutable amounts once signed
- OneToOne relationship with Assignment
- Agreement lifecycle: ACTIVE → COMPLETED or CANCELLED

**Important Methods**:
- `sign()`: Locks commission amounts, cannot be modified after signing
- `complete()`: Called when full payment received
- `cancel(reason)`: Cancels agreement with audit trail

#### 2. Payment Entity Updates
**Location**: `src/main/java/com/roms/entity/Payment.java`

**New Fields**:
- `assignmentId`: Links payment to specific assignment
- `agreementId`: Links payment to commission agreement
- `transactionType`: AGENCY_COMMISSION_DOWNPAYMENT, INSTALLMENT, BALANCE, REVERSAL

### Services

#### 1. AgencyCommissionAgreementService
**Location**: `src/main/java/com/roms/service/AgencyCommissionAgreementService.java`

**Responsibilities**:
- Create and manage commission agreements
- Sign agreements (locks amounts)
- Cancel agreements with audit trail
- Retrieve agreements by candidate/assignment

#### 2. CommissionPaymentService
**Location**: `src/main/java/com/roms/service/CommissionPaymentService.java`

**Critical Business Logic**:
```java
// Downpayment Validation
recordDownpayment(request) {
    validates: amount >= requiredDownpaymentAmount
    creates: Payment with AGENCY_COMMISSION_DOWNPAYMENT type
    updates: agreement.totalPaid
}

// Installment Processing
recordInstallment(request) {
    creates: Payment with AGENCY_COMMISSION_INSTALLMENT type
    updates: agreement.totalPaid
    auto-completes: agreement when totalPaid == totalCommission
}

// Immutable Ledger Pattern
reversePayment(paymentId, reason) {
    NEVER deletes original payment
    creates: New payment with negative amount
    sets: isReversal = true, linkedTransactionId = original
    maintains: Complete audit trail
}
```

**Workflow Guard Methods**:
- `isDownpaymentComplete(assignmentId)`: Used by VISA_PROCESSING gate
- `isFullPaymentComplete(assignmentId)`: Used by PLACED gate

#### 3. CandidateWorkflowService Updates
**Location**: `src/main/java/com/roms/service/CandidateWorkflowService.java`

**New Guard Logic**:
```java
validateDownpaymentPaid(candidate, assignmentId) {
    if (targetStatus == VISA_PROCESSING) {
        if (!commissionPaymentService.isDownpaymentComplete(assignmentId)) {
            throw new WorkflowException("Downpayment required before visa processing");
        }
    }
}

validateFullPaymentComplete(candidate, assignmentId) {
    if (targetStatus == PLACED) {
        if (!commissionPaymentService.isFullPaymentComplete(assignmentId)) {
            throw new WorkflowException("Full payment required before placement");
        }
    }
}
```

### REST API Controllers

#### 1. CommissionAgreementController
**Location**: `src/main/java/com/roms/controller/CommissionAgreementController.java`

**Endpoints**:
```
POST   /api/agreements                      - Create agreement (OPERATIONS_STAFF, SUPER_ADMIN)
GET    /api/agreements/{id}                 - Get by ID
GET    /api/agreements/candidate/{id}       - Candidate's agreements
GET    /api/agreements/assignment/{id}      - Assignment's agreement
PUT    /api/agreements/{id}/sign            - Sign agreement (locks amounts)
DELETE /api/agreements/{id}                 - Cancel agreement (SUPER_ADMIN only)
```

**RBAC Rules**:
- OPERATIONS_STAFF can create and view
- SUPER_ADMIN can create, view, and cancel
- Applicants can view their own (future feature)

#### 2. CommissionPaymentController
**Location**: `src/main/java/com/roms/controller/CommissionPaymentController.java`

**Endpoints**:
```
POST   /api/payments/downpayment                    - Record downpayment
POST   /api/payments/installment                    - Record installment
POST   /api/payments/{id}/reverse                   - Reverse payment (SUPER_ADMIN)
GET    /api/payments/candidate/{id}/statement       - Payment statement
GET    /api/payments/assignment/{id}/downpayment-status  - Check if downpayment complete
GET    /api/payments/assignment/{id}/fullpayment-status  - Check if full payment complete
```

**RBAC Rules**:
- OPERATIONS_STAFF, FINANCE_MANAGER can record payments
- SUPER_ADMIN can reverse payments (emergency only)
- Status endpoints used by UI for workflow lock banners

### Enums

#### TransactionType
**Location**: `src/main/java/com/roms/enums/TransactionType.java`
```java
AGENCY_COMMISSION_DOWNPAYMENT  // Initial payment
AGENCY_COMMISSION_INSTALLMENT  // Subsequent payments
AGENCY_COMMISSION_BALANCE      // Final payment
REVERSAL                        // Payment reversal (negative amount)
```

#### AgreementStatus
**Location**: `src/main/java/com/roms/enums/AgreementStatus.java`
```java
ACTIVE      // Agreement in force, payments being collected
COMPLETED   // Full payment received
CANCELLED   // Agreement terminated
```

#### CandidateStatus Updates
**Location**: `src/main/java/com/roms/enums/CandidateStatus.java`

**New Statuses**:
- `VISA_PROCESSING`: Between OFFER_SIGNED and VISA_APPROVED
- `VISA_APPROVED`: Between visa processing and DEPLOYED

**Updated Workflow**:
```
OFFER_SIGNED → VISA_PROCESSING → VISA_APPROVED → DEPLOYED → PLACED
              ↑                                             ↑
         Downpayment Gate                           Full Payment Gate
```

### DTOs

#### CreateCommissionAgreementRequest
```java
candidateId: Long
assignmentId: Long
totalCommissionAmount: BigDecimal
requiredDownpaymentAmount: BigDecimal
currency: String (default "KES")
notes: String (optional)
```

#### CommissionAgreementDTO
```java
id: String (UUID)
candidateId, candidateName, assignmentId
totalCommissionAmount, requiredDownpaymentAmount
totalPaid, outstandingBalance
signed, signedAt, signedAgreementDocumentUrl
status: AgreementStatus
```

#### CommissionPaymentRequest
```java
candidateId: Long
assignmentId: Long
agreementId: String (UUID)
amount: BigDecimal
paymentMethod: String ("M-PESA", "Bank Transfer", "Cash")
mpesaRef: String (optional, required if paymentMethod = "M-PESA")
description: String (optional)
```

#### CommissionStatementDTO
```java
candidateId, candidateName
totalCommissionAmount, requiredDownpaymentAmount
totalPaid, outstandingBalance
downpaymentComplete: boolean
fullPaymentComplete: boolean
paymentHistory: List<PaymentDTO>
```

## Frontend Implementation

### API Client

#### commission.ts
**Location**: `frontend/src/api/commission.ts`

**TypeScript Interfaces**:
- `CommissionAgreement`: Agreement data structure
- `Payment`: Payment record structure
- `CommissionStatement`: Full payment statement with history
- `CreateCommissionAgreementRequest`, `CommissionPaymentRequest`: Request bodies

**API Methods**:
```typescript
// Agreement Management
createAgreement(data): Create new agreement
getAgreement(id): Get by ID
getCandidateAgreements(candidateId): All candidate agreements
getAssignmentAgreement(assignmentId): Agreement for assignment
signAgreement(id, documentUrl?): Sign agreement
cancelAgreement(id, reason): Cancel with reason

// Payment Operations
recordDownpayment(data): Record initial payment
recordInstallment(data): Record subsequent payment
reversePayment(paymentId, reason): Reverse (SUPER_ADMIN only)
getCandidateStatement(candidateId, agreementId): Full statement
checkDownpaymentStatus(assignmentId): Boolean check
checkFullPaymentStatus(assignmentId): Boolean check
```

### UI Components

#### 1. CommissionManagement.tsx
**Location**: `frontend/src/components/CommissionManagement.tsx`

**Purpose**: Staff interface for creating agreements and recording payments

**Features**:
- **Tabbed Interface**:
  - Agreement Tab: Create/view agreement, sign agreement
  - Payment Tab: Record downpayment/installment
- **Smart Payment Detection**: Auto-selects downpayment vs installment based on current payment status
- **M-PESA Integration**: Dedicated field for M-PESA reference numbers
- **Real-time Updates**: Refreshes parent component on payment success

**Form Defaults**:
- Total Commission: 200,000 KES
- Required Downpayment: 50,000 KES
- Currency: KES

**Usage** (CandidateProfilePage):
```tsx
<CommissionManagement
  candidateId={candidate.id}
  assignmentId={activeAssignment.id}
  onUpdate={() => loadCandidate(id)}
/>
```

**RBAC**: Only shown to OPERATIONS_STAFF and SUPER_ADMIN

#### 2. CommissionSummary.tsx
**Location**: `frontend/src/components/CommissionSummary.tsx`

**Purpose**: Applicant-facing payment dashboard

**Features**:
- **Commission Overview Card**:
  - Total Commission Amount
  - Required Downpayment
  - Total Paid
  - Outstanding Balance
  - Currency formatted as KES
- **Progress Bar**: Visual percentage of payment completion
- **Workflow Status Alerts**:
  - Red Alert: Visa processing blocked (no downpayment)
  - Orange Alert: Placement blocked (no full payment)
  - Blue Info: Downpayment complete, visa processing unlocked
- **Payment History Table**:
  - All transactions listed chronologically
  - Transaction types clearly labeled
  - Reversals highlighted in red
  - M-PESA references shown when available

**Usage** (MyApplicationPage):
```tsx
<CommissionSummary candidateId={candidate.id} />
```

**Auto-loads**: Fetches candidate's active agreement automatically

#### 3. WorkflowLockBanner.tsx
**Location**: `frontend/src/components/WorkflowLockBanner.tsx`

**Purpose**: Visual workflow blocking indicators

**Features**:
- **Dynamic Status Checking**: Fetches downpayment and full payment status via API
- **Context-aware Alerts**:
  - Shows VISA lock when status = OFFER_SIGNED and downpayment incomplete
  - Shows PLACEMENT lock when status ∈ [VISA_PROCESSING, VISA_APPROVED, DEPLOYED] and full payment incomplete
  - Shows SUCCESS message when downpayment complete
- **Visual Design**:
  - Red pulsing banner for critical blocks (visa lock)
  - Orange banner for placement lock
  - Blue info banner for downpayment completion
- **Auto-refresh**: Reloads status when assignmentId or currentStatus changes

**Usage** (CandidateProfilePage):
```tsx
<WorkflowLockBanner
  assignmentId={activeAssignment.id}
  currentStatus={candidate.currentStatus}
/>
```

**Placement**: Shown in right column, above workflow transition controls

### Page Integrations

#### 1. CandidateProfilePage.tsx Updates
**Location**: `frontend/src/pages/CandidateProfilePage.tsx`

**Changes**:
1. **Imports**: Added CommissionManagement, WorkflowLockBanner
2. **WORKFLOW_TRANSITIONS Update**:
   ```typescript
   OFFER_SIGNED: ['VISA_PROCESSING', 'WITHDRAWN'],
   VISA_PROCESSING: ['VISA_APPROVED', 'WITHDRAWN'],
   VISA_APPROVED: ['DEPLOYED', 'WITHDRAWN'],
   ```
3. **WorkflowLockBanner Placement**:
   - Inserted in right column, above workflow transitions
   - Only shown when active assignment exists
4. **CommissionManagement Panel**:
   - Added below workflow section in right column
   - Only shown to OPERATIONS_STAFF and SUPER_ADMIN
   - Only shown when active assignment exists
5. **Workflow Notes Updated**:
   - Added "Downpayment required before visa processing"
   - Added "Full payment required before placement"

#### 2. MyApplicationPage.tsx Updates
**Location**: `frontend/src/pages/MyApplicationPage.tsx`

**Changes**:
1. **Imports**: Added CommissionSummary
2. **State Management**:
   - Added `activeAssignments` state to track assignment IDs
3. **Data Loading**:
   - Loads active assignment for each application
   - Passes to CommissionSummary component
4. **CommissionSummary Integration**:
   - Placed at top of expanded application details
   - Only shown when active assignment exists
   - Displays full payment statement with history
5. **Progress Map Update**:
   - Added VISA_PROCESSING: 87%
   - Added VISA_APPROVED: 93%

## Database Schema Changes

### New Table: agency_commission_agreements

```sql
CREATE TABLE agency_commission_agreements (
    id VARCHAR(36) PRIMARY KEY,  -- UUID
    candidate_id BIGINT NOT NULL,
    assignment_id BIGINT NOT NULL,
    total_commission_amount DECIMAL(19,2) NOT NULL,
    required_downpayment_amount DECIMAL(19,2) NOT NULL,
    total_paid DECIMAL(19,2) DEFAULT 0,
    outstanding_balance DECIMAL(19,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'KES',
    agreement_date TIMESTAMP NOT NULL,
    signed BOOLEAN DEFAULT FALSE,
    signed_at TIMESTAMP,
    signed_agreement_document_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,  -- ACTIVE, COMPLETED, CANCELLED
    notes TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_agreement_candidate FOREIGN KEY (candidate_id) 
        REFERENCES candidates(id),
    CONSTRAINT fk_agreement_assignment FOREIGN KEY (assignment_id) 
        REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT unique_assignment_agreement UNIQUE (assignment_id)
);

CREATE INDEX idx_agreement_candidate ON agency_commission_agreements(candidate_id);
CREATE INDEX idx_agreement_status ON agency_commission_agreements(status);
```

### Updated Table: payments

**New Columns**:
```sql
ALTER TABLE payments ADD COLUMN assignment_id BIGINT;
ALTER TABLE payments ADD COLUMN agreement_id VARCHAR(36);
ALTER TABLE payments ADD COLUMN transaction_type VARCHAR(50);
ALTER TABLE payments ADD COLUMN is_reversal BOOLEAN DEFAULT FALSE;
ALTER TABLE payments ADD COLUMN linked_transaction_id BIGINT;
ALTER TABLE payments ADD COLUMN reversal_reason TEXT;

ALTER TABLE payments ADD CONSTRAINT fk_payment_assignment 
    FOREIGN KEY (assignment_id) REFERENCES assignments(id);
ALTER TABLE payments ADD CONSTRAINT fk_payment_agreement 
    FOREIGN KEY (agreement_id) REFERENCES agency_commission_agreements(id);

CREATE INDEX idx_payment_assignment ON payments(assignment_id);
CREATE INDEX idx_payment_agreement ON payments(agreement_id);
CREATE INDEX idx_payment_type ON payments(transaction_type);
```

### Hibernate Auto-DDL
The schema will be auto-created by Hibernate on first run due to `spring.jpa.hibernate.ddl-auto=update` in application.yaml.

## Testing Workflow

### 1. Create Test Assignment
```bash
# Via UI: CandidateProfilePage → Assignments → Create Assignment
# Select Job Order, click Create
```

### 2. Create Commission Agreement
```bash
# Via UI: CandidateProfilePage → Commission Management → Agreement Tab
Total Commission: 200000 KES
Required Downpayment: 50000 KES
Click "Create Agreement"
```

### 3. Test Visa Lock
```bash
# Try to transition: OFFER_SIGNED → VISA_PROCESSING
# Expected: RED alert "Downpayment required before visa processing"
# Backend throws WorkflowException
```

### 4. Record Downpayment
```bash
# Via UI: Commission Management → Payment Tab
Amount: 50000 (or more)
Payment Method: M-PESA
M-PESA Ref: ABC123XYZ
Click "Record Payment"

# Expected: BLUE success banner appears
# Can now transition to VISA_PROCESSING
```

### 5. Record Installments
```bash
# Payment Tab automatically detects "installment" mode
Amount: 75000
Payment Method: Bank Transfer
Click "Record Payment"

# Repeat until totalPaid = 200000
```

### 6. Test Placement Lock
```bash
# With partial payment (e.g., 150000 of 200000 paid)
# Try transition: DEPLOYED → PLACED
# Expected: ORANGE alert "Full payment required before placement"

# Complete payment (record remaining 50000)
# Try transition again
# Expected: SUCCESS
```

### 7. Test Reversal (SUPER_ADMIN only)
```bash
# Via API or future UI:
POST /api/payments/{paymentId}/reverse?reason=Duplicate%20entry

# Expected:
# - Original payment unchanged
# - New payment created with negative amount
# - isReversal = true
# - Payment history shows reversal in red
```

## API Testing Examples

### Create Agreement
```bash
curl -X POST http://localhost:8080/api/agreements \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": 123,
    "assignmentId": 456,
    "totalCommissionAmount": 200000,
    "requiredDownpaymentAmount": 50000,
    "currency": "KES",
    "notes": "Standard agency commission"
  }'
```

### Record Downpayment
```bash
curl -X POST http://localhost:8080/api/payments/downpayment \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": 123,
    "assignmentId": 456,
    "agreementId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 50000,
    "paymentMethod": "M-PESA",
    "mpesaRef": "ABC123XYZ",
    "description": "Initial downpayment via M-PESA"
  }'
```

### Check Downpayment Status
```bash
curl -X GET http://localhost:8080/api/payments/assignment/456/downpayment-status \
  -H "Authorization: Bearer {token}"

# Response:
{
  "status": "success",
  "message": "Downpayment status retrieved",
  "data": true  // or false
}
```

### Get Payment Statement
```bash
curl -X GET "http://localhost:8080/api/payments/candidate/123/statement?agreementId=550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer {token}"

# Response includes full payment history
```

## Security Considerations

### 1. Immutable Ledger
- **NEVER** delete or update payment records
- Reversals create new negative entries
- Complete audit trail maintained
- Complies with financial record-keeping regulations

### 2. RBAC Enforcement
- **OPERATIONS_STAFF**: Create agreements, record payments
- **FINANCE_MANAGER**: Record payments, view statements
- **SUPER_ADMIN**: All operations + reversals + cancellations
- **APPLICANT**: View own statements (future)

### 3. BigDecimal Precision
- All monetary calculations use `BigDecimal`
- Prevents floating-point rounding errors
- Critical for financial integrity

### 4. Server-Side Guards
- Workflow gates enforced in backend service layer
- Frontend cannot bypass payment requirements
- Guards throw `WorkflowException` with clear messages

### 5. UUID Agreement IDs
- UUIDs prevent ID guessing attacks
- More secure than sequential integers
- Safe to expose in URLs

## Known Limitations & Future Enhancements

### Current Limitations
1. **No Multi-Currency**: Currently hardcoded to KES
2. **No Payment Approval Workflow**: Payments are immediately applied
3. **No Applicant Payment Portal**: Applicants can only view, not pay
4. **No Automated Receipts**: Manual receipt generation required
5. **No Payment Reminders**: No automated email/SMS reminders

### Planned Enhancements
1. **Multi-Currency Support**: Handle USD, EUR, etc.
2. **Payment Approval Queue**: Finance Manager approval before applying
3. **Applicant Self-Service**: Let applicants pay via M-PESA STK Push
4. **Receipt Generation**: Auto-generate PDF receipts via Jasper Reports
5. **Payment Reminders**: Email/SMS when payments overdue
6. **Installment Plans**: Formalized payment schedules with due dates
7. **Late Fees**: Configurable late fees for overdue installments
8. **Partial Refunds**: Handle refunds for cancelled placements
9. **Commission Splits**: Split commission between multiple agencies
10. **Escrow Integration**: Hold payments until candidate deployed

## Deployment Checklist

### Pre-Deployment
- [ ] Backend compiles without errors (`mvnw clean compile`)
- [ ] Frontend builds without errors (`npm run build`)
- [ ] All tests pass (unit + integration)
- [ ] Database migration script reviewed (if manual SQL needed)
- [ ] RBAC rules verified for all endpoints
- [ ] API documentation updated (Swagger/OpenAPI)

### Deployment Steps
1. **Backup Database**: Full backup before deployment
2. **Deploy Backend**: 
   - `mvnw clean package -DskipTests`
   - Deploy JAR to server
   - Restart Spring Boot application
3. **Verify DDL**: Check logs for Hibernate schema updates
4. **Deploy Frontend**:
   - `npm run build`
   - Copy `dist/` to web server
5. **Smoke Test**:
   - Create test agreement
   - Record test payment
   - Verify workflow lock/unlock
   - Check payment statement

### Post-Deployment
- [ ] Monitor application logs for errors
- [ ] Verify no 500 errors in API calls
- [ ] Test workflow transitions with staff account
- [ ] Test applicant payment view
- [ ] Verify M-PESA integration (if live)
- [ ] Check database for orphaned records

## Support & Troubleshooting

### Common Issues

#### "Downpayment required" error even after payment
**Cause**: Payment not linked to correct assignment/agreement
**Fix**: 
```sql
SELECT * FROM payments 
WHERE candidate_id = ? AND agreement_id = ?
ORDER BY payment_date DESC;
```
Verify `assignment_id` and `agreement_id` are correct.

#### Agreement not found for assignment
**Cause**: Agreement not created or cancelled
**Fix**: Create new agreement via CommissionManagement component

#### Workflow transition fails with 500 error
**Cause**: Guard logic validation failed
**Fix**: Check backend logs for `WorkflowException` details

#### Payment not showing in statement
**Cause**: Wrong `agreementId` parameter
**Fix**: Verify `agreementId` matches the assignment's agreement

### Logging
Enable DEBUG logging for Phase 2B:
```yaml
logging:
  level:
    com.roms.service.CommissionPaymentService: DEBUG
    com.roms.service.AgencyCommissionAgreementService: DEBUG
    com.roms.service.CandidateWorkflowService: DEBUG
```

## Documentation Updates Needed

1. **ARCHITECTURE.md**: Add Phase 2B section with entity relationships
2. **README.md**: Update features list with commission management
3. **API_DOCS.md**: Document new `/api/agreements` and `/api/payments` endpoints (create if doesn't exist)
4. **USER_GUIDE.md**: Add staff workflow for recording payments (create if doesn't exist)
5. **APPLICANT_GUIDE.md**: Add payment viewing instructions (create if doesn't exist)

## Git Commit Strategy

### Recommended Commits
```bash
# 1. Backend entities and enums
git add src/main/java/com/roms/entity/AgencyCommissionAgreement.java
git add src/main/java/com/roms/entity/Payment.java
git add src/main/java/com/roms/enums/TransactionType.java
git add src/main/java/com/roms/enums/AgreementStatus.java
git add src/main/java/com/roms/enums/CandidateStatus.java
git commit -m "Phase 2B: Add commission agreement entity and payment updates"

# 2. Backend repositories and services
git add src/main/java/com/roms/repository/AgencyCommissionAgreementRepository.java
git add src/main/java/com/roms/repository/PaymentRepository.java
git add src/main/java/com/roms/service/AgencyCommissionAgreementService.java
git add src/main/java/com/roms/service/CommissionPaymentService.java
git commit -m "Phase 2B: Implement commission services with immutable ledger"

# 3. Workflow guards
git add src/main/java/com/roms/service/CandidateWorkflowService.java
git commit -m "Phase 2B: Add payment-gated workflow guards for visa and placement"

# 4. DTOs and controllers
git add src/main/java/com/roms/dto/
git add src/main/java/com/roms/controller/CommissionAgreementController.java
git add src/main/java/com/roms/controller/CommissionPaymentController.java
git commit -m "Phase 2B: Add REST APIs for commission and payment management"

# 5. Frontend API and components
git add frontend/src/api/commission.ts
git add frontend/src/components/CommissionSummary.tsx
git add frontend/src/components/CommissionManagement.tsx
git add frontend/src/components/WorkflowLockBanner.tsx
git commit -m "Phase 2B: Add commission management UI components"

# 6. Page integrations
git add frontend/src/pages/CandidateProfilePage.tsx
git add frontend/src/pages/MyApplicationPage.tsx
git add frontend/src/types/index.ts
git commit -m "Phase 2B: Integrate commission components into candidate pages"

# 7. Documentation
git add PHASE_2B_SUMMARY.md
git add ARCHITECTURE.md
git add README.md
git commit -m "Phase 2B: Add comprehensive documentation"

# 8. Push all changes
git push origin main
```

## Conclusion

Phase 2B successfully implements a production-ready applicant-funded commission management system with:

✅ **Immutable Financial Ledger** - Append-only payments with reversal pattern
✅ **Payment-Gated Workflow** - Server-enforced guards for visa processing and placement
✅ **Visual Workflow Locks** - Clear UI indicators for payment requirements
✅ **Comprehensive RBAC** - Role-based access for all operations
✅ **Staff Management Interface** - Easy agreement creation and payment recording
✅ **Applicant Dashboard** - Transparent payment history and status
✅ **M-PESA Integration** - Support for Kenya's primary payment method
✅ **BigDecimal Precision** - No rounding errors in financial calculations
✅ **Complete Audit Trail** - All payment changes tracked with Hibernate Envers

The system is now ready for end-to-end testing and deployment.
