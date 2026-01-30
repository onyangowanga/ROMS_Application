# ROMS - Complete Project Documentation

**Recruitment Operations Management System**  
**Version**: 2.1.0 | **Status**: Phase 2B Complete ✅ | **Last Updated**: January 29, 2026

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [System Architecture](#system-architecture)
4. [Phase 1: Core Recruitment System](#phase-1-core-recruitment-system)
5. [Phase 2A: Employer-Funded Commission](#phase-2a-employer-funded-commission)
6. [Phase 2B: Applicant-Funded Commission](#phase-2b-applicant-funded-commission)
7. [Database Schema](#database-schema)
8. [API Documentation](#api-documentation)
9. [Development Setup](#development-setup)
10. [Deployment Guide](#deployment-guide)

---

## Project Overview

ROMS is an enterprise-grade Recruitment Operations Management System designed to streamline the recruitment workflow from initial candidate application through to deployment and payment management. The system features role-based access control, comprehensive audit trails, document management, and multi-phase commission tracking.

### Business Context
- **Target Users**: Recruitment agencies managing overseas employment placements
- **Geographic Focus**: Kenya (primary) with Middle East placements
- **Payment Integration**: M-PESA, bank transfers, cash handling
- **Compliance**: Audit trails for financial transactions and candidate status changes

### Key Capabilities
✅ End-to-end candidate lifecycle management  
✅ Multi-role user authentication (5 roles)  
✅ Job order creation and fulfillment tracking  
✅ Assignment-based candidate-job relationships  
✅ Dual commission models (employer-funded & applicant-funded)  
✅ Payment-gated workflow transitions  
✅ Document management with validation  
✅ Complete audit trail (Hibernate Envers)  
✅ Responsive React frontend with TypeScript  

---

## Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 LTS | Core programming language |
| **Spring Boot** | 3.2.2 | Application framework |
| **Spring Security** | 6.2.1 | Authentication & authorization |
| **Spring Data JPA** | 3.2.2 | Database abstraction |
| **Hibernate** | 6.4.1 | ORM implementation |
| **Hibernate Envers** | 6.4.1 | Audit trail automation |
| **PostgreSQL** | 16-alpine | Relational database |
| **Lombok** | 1.18.32 | Boilerplate reduction |
| **Maven** | 3.9.12 | Build & dependency management |
| **JWT** | 0.12.3 | Token-based authentication |
| **BCrypt** | - | Password hashing |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 18.2.0 | UI framework |
| **TypeScript** | 5.6.2 | Type-safe JavaScript |
| **Vite** | 5.4.21 | Build tool & dev server |
| **React Router** | 7.1.1 | Client-side routing |
| **Axios** | 1.7.9 | HTTP client |
| **Tailwind CSS** | 3.4.17 | Utility-first styling |

### DevOps & Infrastructure
| Technology | Version | Purpose |
|------------|---------|---------|
| **Docker** | Latest | Containerization |
| **Docker Compose** | 2.x | Multi-container orchestration |
| **pgAdmin** | 4 | Database management UI |
| **IntelliJ IDEA** | 2024+ | Primary IDE (Lombok support) |

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     FRONTEND LAYER (React)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Admin Panel │  │ Employer UI  │  │ Applicant UI │          │
│  │  (Dashboard) │  │ (Jobs/Apply) │  │ (Profile)    │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                    │
│         └─────────────────┴─────────────────┘                    │
│                           │                                      │
│                     JWT Bearer Token                             │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                     HTTPS REST API
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                   SPRING SECURITY LAYER                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │         JwtAuthenticationFilter                          │   │
│  │  • Validate JWT token                                    │   │
│  │  • Extract user details & roles                          │   │
│  │  • Set SecurityContext                                   │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────┼──────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                      CONTROLLER LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Auth      │  │  Candidate   │  │  Assignment  │          │
│  │ Controller   │  │  Controller  │  │  Controller  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Commission   │  │  Commission  │  │   Document   │          │
│  │  Agreement   │  │   Payment    │  │  Controller  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                  │
│         @PreAuthorize("hasRole('SUPER_ADMIN')")                 │
└───────────────────────────┼──────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                       SERVICE LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │      CandidateWorkflowService (State Machine)            │   │
│  │  • Guard logic validation                                │   │
│  │  • Payment-gated transitions                             │   │
│  │  • Status change audit trail                             │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Commission   │  │ Commission   │  │  Assignment  │          │
│  │  Agreement   │  │   Payment    │  │   Service    │          │
│  │   Service    │  │   Service    │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└───────────────────────────┼──────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                REPOSITORY LAYER (Spring Data JPA)                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │     User     │  │  Candidate   │  │  Assignment  │          │
│  │  Repository  │  │  Repository  │  │  Repository  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Commission  │  │   Payment    │  │   Document   │          │
│  │  Agreement   │  │  Repository  │  │  Repository  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                    Hibernate ORM + Envers
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                   DATABASE LAYER (PostgreSQL)                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Main Tables                            │   │
│  │  • users                  • candidates                   │   │
│  │  • job_orders             • employers                    │   │
│  │  • assignments            • payments                     │   │
│  │  • agency_commission_agreements                          │   │
│  │  • candidate_documents                                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │            Audit Tables (Hibernate Envers)               │   │
│  │  • users_aud              • candidates_aud               │   │
│  │  • assignments_aud        • payments_aud                 │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Core Recruitment System

### Phase 1 Overview
**Commit**: Initial implementation (January 2026)  
**Goal**: Establish foundational recruitment operations platform

### Phase 1 Features

#### 1. Authentication & Authorization
**Implementation**: JWT-based authentication with role-based access control

**User Roles**:
| Role | Permissions | Typical Use Case |
|------|-------------|------------------|
| `SUPER_ADMIN` | Full system access, can cancel PLACED assignments | System administrator |
| `FINANCE_MANAGER` | View financials, manage payments | Accounting department |
| `OPERATIONS_STAFF` | Manage candidates, assignments, jobs | Recruitment officers |
| `APPLICANT` | Submit applications, upload documents | Job seekers |
| `EMPLOYER` | Post jobs, view assigned candidates | Client companies |

**Key Files**:
- [JwtAuthenticationFilter.java](src/main/java/com/roms/config/JwtAuthenticationFilter.java) - Token validation
- [SecurityConfig.java](src/main/java/com/roms/config/SecurityConfig.java) - Security rules
- [AuthController.java](src/main/java/com/roms/controller/AuthController.java) - Login/registration
- [AuthContext.tsx](frontend/src/context/AuthContext.tsx) - Frontend auth state

**Authentication Flow**:
```
1. User submits credentials → AuthController.login()
2. Validate password → BCrypt.checkPassword()
3. Generate JWT tokens → JwtUtil.generateToken()
4. Return { accessToken, refreshToken, user }
5. Frontend stores in AuthContext + localStorage
6. Subsequent requests include: Authorization: Bearer <token>
7. JwtAuthenticationFilter validates token on each request
```

#### 2. Candidate Management

**Lifecycle States**:
```
APPLIED → DOCUMENTS_SUBMITTED → INTERVIEW_SCHEDULED → 
INTERVIEW_PASSED → MEDICAL_SCHEDULED → MEDICAL_PASSED → 
OFFER_ISSUED → OFFER_SIGNED → DOCUMENTS_VERIFIED → 
DEPLOYED → PLACED → COMPLETED
```

**Key Features**:
- ✅ Auto-generated internal reference number (format: `ROMS-YYYY-NNNN`)
- ✅ Soft delete with unique constraint management
- ✅ Passport number uniqueness (excluding soft-deleted records)
- ✅ Email uniqueness validation
- ✅ Job application workflow
- ✅ Status transition validation

**Key Files**:
- [Candidate.java](src/main/java/com/roms/entity/Candidate.java) - Entity model
- [CandidateWorkflowService.java](src/main/java/com/roms/service/CandidateWorkflowService.java) - State machine
- [CandidateController.java](src/main/java/com/roms/controller/CandidateController.java) - REST API
- [CandidatesPage.tsx](frontend/src/pages/CandidatesPage.tsx) - Admin UI
- [MyApplicationPage.tsx](frontend/src/pages/MyApplicationPage.tsx) - Applicant UI

**Important Methods**:
```java
// CandidateWorkflowService.java
updateStatus(candidateId, targetStatus) {
    1. validateTransition(currentStatus → targetStatus)
    2. checkBusinessRules(candidate, targetStatus)
    3. updateCandidateStatus()
    4. auditTrail.record() // Hibernate Envers automatic
}
```

#### 3. Job Order Management

**Job Order Lifecycle**:
```
OPEN → FILLED → CLOSED
     ↘ CANCELLED
```

**Key Features**:
- ✅ Position title, description, requirements
- ✅ Headcount tracking (requested vs. filled)
- ✅ Employer association
- ✅ Location tracking (e.g., Saudi Arabia, UAE)
- ✅ Fulfillment validation (cannot exceed headcount)
- ✅ Status management

**Key Files**:
- [JobOrder.java](src/main/java/com/roms/entity/JobOrder.java) - Entity model
- [JobOrderService.java](src/main/java/com/roms/service/JobOrderService.java) - Business logic
- [JobOrderController.java](src/main/java/com/roms/controller/JobOrderController.java) - REST API
- [JobsPage.tsx](frontend/src/pages/JobsPage.tsx) - Job listing UI

**Critical Validation**:
```java
// JobOrderService.java
validateHeadcount(jobOrderId, newAssignment) {
    currentFilled = assignmentRepository.countByJobOrderAndStatus(PLACED)
    if (currentFilled >= jobOrder.headcount) {
        throw new ValidationException("Job order capacity exceeded")
    }
}
```

#### 4. Document Management

**Document Types**:
- `PASSPORT` - Travel document
- `MEDICAL` - Health certificate
- `VISA` - Entry permit
- `OFFER_LETTER` - Job offer
- `CONTRACT` - Employment agreement
- `OTHER` - Miscellaneous

**Storage Options**:
- **Local Storage** (Development): `uploads/` directory
- **Google Cloud Storage** (Production): Configured via `credentials.json`

**Key Features**:
- ✅ Multi-file upload support
- ✅ Metadata tracking (upload date, file size, type)
- ✅ Role-based access (applicants upload to own records only)
- ✅ File type validation
- ✅ Expiry date tracking (future feature)

**Key Files**:
- [Document.java](src/main/java/com/roms/entity/Document.java) - Entity model
- [DocumentService.java](src/main/java/com/roms/service/DocumentService.java) - Upload logic
- [DocumentController.java](src/main/java/com/roms/controller/DocumentController.java) - REST API
- [CandidateProfilePage.tsx](frontend/src/pages/CandidateProfilePage.tsx) - Upload UI

#### 5. Frontend Application

**Framework**: React 18 + TypeScript + Vite

**Key Pages**:
| Page | Route | Purpose |
|------|-------|---------|
| Login | `/login` | Authentication |
| Dashboard | `/dashboard` | Role-based landing page |
| Candidates | `/candidates` | Candidate listing (admin) |
| Candidate Profile | `/candidates/:id` | Individual candidate details |
| Jobs | `/jobs` | Job order listing |
| My Application | `/my-application` | Applicant self-service |
| Users | `/users` | User management (admin) |

**Key Components**:
- [Layout.tsx](frontend/src/components/Layout.tsx) - Navigation shell
- [ProtectedRoute.tsx](frontend/src/components/ProtectedRoute.tsx) - Route guards
- [StatusBadge.tsx](frontend/src/components/StatusBadge.tsx) - Visual status indicators

**API Integration**:
- [axios.ts](frontend/src/api/axios.ts) - HTTP client with JWT interceptors
- [auth.ts](frontend/src/api/auth.ts) - Authentication API
- [candidates.ts](frontend/src/api/candidates.ts) - Candidate API
- [jobs.ts](frontend/src/api/jobs.ts) - Job order API

### Phase 1 Challenges & Solutions

#### Challenge 1: Circular Reference in JSON Serialization
**Problem**: `Candidate ↔ JobOrder ↔ Employer` bidirectional relationships caused infinite recursion

**Solution**:
```java
@Entity
@JsonIgnoreProperties({"candidates", "assignments"})
public class JobOrder {
    @OneToMany(mappedBy = "jobOrder")
    private List<Assignment> assignments;
}
```

#### Challenge 2: Unique Constraints with Soft Delete
**Problem**: Deleted candidates with same passport number prevented re-registration

**Solution**:
```sql
-- Partial index: uniqueness only for non-deleted records
CREATE UNIQUE INDEX idx_candidate_passport_active 
ON candidates(passport_number) 
WHERE deleted = false;
```

#### Challenge 3: Lazy-Loading Proxy Issues
**Problem**: Jackson serialization failed on Hibernate proxies

**Solution**:
```java
// Use DTOs for API responses
public class CandidateDTO {
    public static CandidateDTO fromEntity(Candidate candidate) {
        return CandidateDTO.builder()
            .id(candidate.getId())
            .fullName(candidate.getFullName())
            // Explicitly map fields, avoid lazy collections
            .build();
    }
}
```

---

## Phase 2A: Employer-Funded Commission

### Phase 2A Overview
**Commit**: de011d8 (January 2026)  
**Goal**: Implement employer-funded commission model with assignment tracking

### Phase 2A Features

#### 1. Assignment Module
**Purpose**: Replace direct many-to-many relationship between Candidate and JobOrder with explicit Assignment entity

**Why Assignment Entity?**
- ✅ Track individual placement history
- ✅ Support multiple assignments per candidate (over time)
- ✅ Prevent duplicate active assignments
- ✅ Enable commission tracking per assignment
- ✅ Maintain audit trail of assignment changes

**Assignment Lifecycle**:
```
ASSIGNED → OFFERED → PLACED
                  ↘ CANCELLED
```

**Key Business Rules**:
1. **One Active Assignment**: A candidate can only have ONE active assignment (not CANCELLED, not COMPLETED)
2. **PLACED Protection**: Only SUPER_ADMIN can cancel PLACED assignments (accounting implications)
3. **Headcount Tracking**: Job orders track filled positions via PLACED assignments
4. **Status Synchronization**: Assignment status impacts candidate status

**Key Files**:
- [Assignment.java](src/main/java/com/roms/entity/Assignment.java) - Entity model
- [AssignmentService.java](src/main/java/com/roms/service/AssignmentService.java) - Business logic
- [AssignmentController.java](src/main/java/com/roms/controller/AssignmentController.java) - REST API

**Critical Methods**:
```java
// AssignmentService.java
createAssignment(candidateId, jobOrderId) {
    1. validateCandidateEligibility()
    2. validateJobOrderCapacity()
    3. checkForActiveAssignments() // Prevent duplicates
    4. createAssignmentRecord(status = ASSIGNED)
    5. updateJobOrderFulfillment()
}

updateAssignmentStatus(assignmentId, targetStatus) {
    1. validateStatusTransition()
    2. checkRolePermissions() // PLACED cancellation
    3. updateAssignment()
    4. synchronizeCandidateStatus()
    5. updateJobOrderHeadcount()
}
```

**Database Uniqueness**:
```sql
-- Prevent duplicate active assignments
CREATE UNIQUE INDEX idx_assignment_candidate_active 
ON assignments(candidate_id) 
WHERE status NOT IN ('CANCELLED', 'COMPLETED');
```

#### 2. Employer-Funded Payment Tracking
**Implementation**: Payment entity extended to support employer-funded commissions

**Payment Types** (Phase 2A):
- `EMPLOYER_COMMISSION` - Employer pays recruitment agency

**Key Features**:
- ✅ Link payments to specific assignments
- ✅ Track payment method (M-PESA, bank, cash)
- ✅ Reference number storage (e.g., M-PESA code)
- ✅ Audit trail for all transactions

**Key Files**:
- [Payment.java](src/main/java/com/roms/entity/Payment.java) - Entity model (updated)
- [PaymentService.java](src/main/java/com/roms/service/PaymentService.java) - Payment processing

### Phase 2A Challenges & Solutions

#### Challenge 1: Multiple Active Assignments
**Problem**: Candidates were being assigned to multiple jobs simultaneously, causing confusion

**Solution**:
```java
// AssignmentService.java
private void validateNoActiveAssignments(Long candidateId) {
    List<Assignment> active = assignmentRepository
        .findByCandidateIdAndStatusNotIn(candidateId, 
            Arrays.asList(AssignmentStatus.CANCELLED, AssignmentStatus.COMPLETED));
    
    if (!active.isEmpty()) {
        throw new DuplicateAssignmentException(
            "Candidate already has active assignment: " + active.get(0).getId()
        );
    }
}
```

#### Challenge 2: Job Order Headcount Accuracy
**Problem**: Headcount calculation was inaccurate when assignments were cancelled

**Solution**:
```java
// JobOrderService.java
public int getCurrentFilled(Long jobOrderId) {
    return assignmentRepository.countByJobOrderIdAndStatus(
        jobOrderId, 
        AssignmentStatus.PLACED // Only count PLACED
    );
}
```

---

## Phase 2B: Applicant-Funded Commission

### Phase 2B Overview
**Commit**: Current (January 2026)  
**Goal**: Implement applicant-funded commission model with payment-gated workflow transitions

### Phase 2B Business Context

**Typical Commission Structure**:
- Total Commission: **200,000 KES**
- Required Downpayment: **50,000 KES** (before visa processing)
- Balance: **150,000 KES** (installments allowed)
- Payment Methods: M-PESA, Bank Transfer, Cash

**Payment Gates**:
1. **VISA_PROCESSING Gate**: Blocks transition from OFFER_SIGNED → VISA_PROCESSING until downpayment paid
2. **PLACEMENT Gate**: Blocks transition from DEPLOYED → PLACED until full commission paid

### Phase 2B Implementation

#### 1. Agency Commission Agreement Entity

**Purpose**: Formalize commission contract between agency and applicant

**Key Features**:
- ✅ UUID primary key (security)
- ✅ Immutable amounts once signed
- ✅ OneToOne relationship with Assignment
- ✅ Agreement lifecycle tracking

**Entity**: [AgencyCommissionAgreement.java](src/main/java/com/roms/entity/AgencyCommissionAgreement.java)

**Schema**:
```java
@Entity
public class AgencyCommissionAgreement {
    @Id
    private UUID id;
    
    @OneToOne
    private Assignment assignment;
    
    @ManyToOne
    private Candidate candidate;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalCommission; // e.g., 200000.00
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal requiredDownpaymentAmount; // e.g., 50000.00
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPaid; // Running total
    
    @Enumerated(EnumType.STRING)
    private AgreementStatus status; // ACTIVE, COMPLETED, CANCELLED
    
    private LocalDateTime signedAt; // Locks amounts
    private LocalDateTime completedAt;
    private String cancellationReason;
}
```

**Critical Methods**:
```java
public void sign() {
    if (this.signedAt != null) {
        throw new IllegalStateException("Agreement already signed");
    }
    this.signedAt = LocalDateTime.now();
    // Amounts are now IMMUTABLE
}

public void complete() {
    this.status = AgreementStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
}

public void cancel(String reason) {
    this.status = AgreementStatus.CANCELLED;
    this.cancellationReason = reason;
}
```

#### 2. Transaction Types

**New Enums**: [TransactionType.java](src/main/java/com/roms/enums/TransactionType.java)

```java
public enum TransactionType {
    EMPLOYER_COMMISSION,              // Phase 2A
    AGENCY_COMMISSION_DOWNPAYMENT,   // Phase 2B (initial payment)
    AGENCY_COMMISSION_INSTALLMENT,   // Phase 2B (partial payment)
    AGENCY_COMMISSION_BALANCE,       // Phase 2B (final payment)
    AGENCY_COMMISSION_REVERSAL       // Phase 2B (payment cancellation)
}
```

#### 3. Commission Payment Service

**Service**: [CommissionPaymentService.java](src/main/java/com/roms/service/CommissionPaymentService.java)

**Core Business Logic**:

##### a) Record Downpayment
```java
public PaymentDTO recordDownpayment(CommissionPaymentRequest request) {
    // 1. Validate agreement exists and is active
    AgencyCommissionAgreement agreement = findActiveAgreement(request.getAgreementId());
    
    // 2. Validate downpayment amount
    if (request.getAmount().compareTo(agreement.getRequiredDownpaymentAmount()) < 0) {
        throw new InsufficientPaymentException(
            "Downpayment must be at least " + agreement.getRequiredDownpaymentAmount()
        );
    }
    
    // 3. Create payment record
    Payment payment = Payment.builder()
        .candidate(agreement.getCandidate())
        .assignment(agreement.getAssignment())
        .agreementId(agreement.getId())
        .amount(request.getAmount())
        .paymentMethod(request.getPaymentMethod())
        .transactionType(TransactionType.AGENCY_COMMISSION_DOWNPAYMENT)
        .referenceNumber(request.getReferenceNumber())
        .build();
    
    paymentRepository.save(payment);
    
    // 4. Update agreement total paid
    BigDecimal newTotal = agreement.getTotalPaid().add(request.getAmount());
    agreement.setTotalPaid(newTotal);
    agreementRepository.save(agreement);
    
    return PaymentDTO.fromEntity(payment);
}
```

##### b) Record Installment
```java
public PaymentDTO recordInstallment(CommissionPaymentRequest request) {
    AgencyCommissionAgreement agreement = findActiveAgreement(request.getAgreementId());
    
    // 1. Validate downpayment already completed
    if (!isDownpaymentComplete(agreement.getAssignment().getId())) {
        throw new WorkflowException("Downpayment must be completed before installments");
    }
    
    // 2. Create installment payment
    Payment payment = Payment.builder()
        .candidate(agreement.getCandidate())
        .assignment(agreement.getAssignment())
        .agreementId(agreement.getId())
        .amount(request.getAmount())
        .paymentMethod(request.getPaymentMethod())
        .transactionType(TransactionType.AGENCY_COMMISSION_INSTALLMENT)
        .referenceNumber(request.getReferenceNumber())
        .build();
    
    paymentRepository.save(payment);
    
    // 3. Update total paid
    BigDecimal newTotal = agreement.getTotalPaid().add(request.getAmount());
    agreement.setTotalPaid(newTotal);
    
    // 4. Auto-complete if fully paid
    if (newTotal.compareTo(agreement.getTotalCommission()) >= 0) {
        agreement.complete();
    }
    
    agreementRepository.save(agreement);
    
    return PaymentDTO.fromEntity(payment);
}
```

##### c) Reverse Payment (Immutable Ledger Pattern)
```java
public PaymentDTO reversePayment(Long paymentId, String reason) {
    Payment originalPayment = findPayment(paymentId);
    
    // NEVER delete the original payment
    // Create a negative entry instead
    Payment reversal = Payment.builder()
        .candidate(originalPayment.getCandidate())
        .assignment(originalPayment.getAssignment())
        .agreementId(originalPayment.getAgreementId())
        .amount(originalPayment.getAmount().negate()) // Negative amount
        .paymentMethod(originalPayment.getPaymentMethod())
        .transactionType(TransactionType.AGENCY_COMMISSION_REVERSAL)
        .isReversal(true)
        .linkedTransactionId(originalPayment.getId())
        .notes("Reversal: " + reason)
        .build();
    
    paymentRepository.save(reversal);
    
    // Update agreement total paid
    AgencyCommissionAgreement agreement = findAgreement(reversal.getAgreementId());
    BigDecimal newTotal = agreement.getTotalPaid().subtract(originalPayment.getAmount());
    agreement.setTotalPaid(newTotal);
    agreementRepository.save(agreement);
    
    return PaymentDTO.fromEntity(reversal);
}
```

##### d) Workflow Guard Methods
```java
public boolean isDownpaymentComplete(Long assignmentId) {
    AgencyCommissionAgreement agreement = findByAssignmentId(assignmentId);
    if (agreement == null) return false;
    
    // Check if total paid >= required downpayment
    return agreement.getTotalPaid()
        .compareTo(agreement.getRequiredDownpaymentAmount()) >= 0;
}

public boolean isFullPaymentComplete(Long assignmentId) {
    AgencyCommissionAgreement agreement = findByAssignmentId(assignmentId);
    if (agreement == null) return false;
    
    // Check if total paid >= total commission
    return agreement.getTotalPaid()
        .compareTo(agreement.getTotalCommission()) >= 0;
}
```

#### 4. Workflow Service Integration

**Service**: [CandidateWorkflowService.java](src/main/java/com/roms/service/CandidateWorkflowService.java)

**Payment Gate Validation**:

```java
public void updateStatus(Long candidateId, CandidateStatus targetStatus, Long assignmentId) {
    Candidate candidate = findCandidate(candidateId);
    
    // Existing validation...
    validateTransition(candidate.getStatus(), targetStatus);
    
    // Phase 2B: Payment gate validation
    validatePaymentGates(candidate, targetStatus, assignmentId);
    
    // Update status...
    candidate.setStatus(targetStatus);
    candidateRepository.save(candidate);
}

private void validatePaymentGates(Candidate candidate, CandidateStatus targetStatus, Long assignmentId) {
    // Gate 1: VISA_PROCESSING requires downpayment
    if (targetStatus == CandidateStatus.VISA_PROCESSING) {
        validateDownpaymentPaid(candidate, assignmentId);
    }
    
    // Gate 2: PLACED requires full payment
    if (targetStatus == CandidateStatus.PLACED) {
        validateFullPaymentComplete(candidate, assignmentId);
    }
}

private void validateDownpaymentPaid(Candidate candidate, Long assignmentId) {
    if (assignmentId == null) {
        throw new IllegalArgumentException("Assignment ID required for payment validation");
    }
    
    if (!commissionPaymentService.isDownpaymentComplete(assignmentId)) {
        throw new WorkflowException(
            "Cannot proceed to VISA_PROCESSING: Downpayment of " + 
            getRequiredDownpayment(assignmentId) + " KES not yet paid"
        );
    }
}

private void validateFullPaymentComplete(Candidate candidate, Long assignmentId) {
    if (assignmentId == null) {
        throw new IllegalArgumentException("Assignment ID required for payment validation");
    }
    
    if (!commissionPaymentService.isFullPaymentComplete(assignmentId)) {
        BigDecimal outstanding = getOutstandingBalance(assignmentId);
        throw new WorkflowException(
            "Cannot proceed to PLACED: Outstanding balance of " + 
            outstanding + " KES must be paid"
        );
    }
}
```

#### 5. REST API Controllers

##### a) Commission Agreement Controller

**Controller**: [CommissionAgreementController.java](src/main/java/com/roms/controller/CommissionAgreementController.java)

**Endpoints**:

```java
@RestController
@RequestMapping("/api/agreements")
public class CommissionAgreementController {
    
    // Create new agreement
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> createAgreement(
        @RequestBody CreateCommissionAgreementRequest request
    ) {
        CommissionAgreementDTO agreement = agreementService.createAgreement(request);
        return ResponseEntity.ok(ApiResponse.success(agreement));
    }
    
    // Get agreement by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> getAgreement(@PathVariable UUID id) {
        CommissionAgreementDTO agreement = agreementService.getAgreementById(id);
        return ResponseEntity.ok(ApiResponse.success(agreement));
    }
    
    // Get candidate's agreements
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> getCandidateAgreements(
        @PathVariable Long candidateId
    ) {
        List<CommissionAgreementDTO> agreements = 
            agreementService.getAgreementsByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success(agreements));
    }
    
    // Get agreement by assignment
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> getAssignmentAgreement(
        @PathVariable Long assignmentId
    ) {
        CommissionAgreementDTO agreement = 
            agreementService.getAgreementByAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(agreement));
    }
    
    // Sign agreement (locks amounts)
    @PutMapping("/{id}/sign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> signAgreement(@PathVariable UUID id) {
        CommissionAgreementDTO agreement = agreementService.signAgreement(id);
        return ResponseEntity.ok(ApiResponse.success(agreement));
    }
    
    // Cancel agreement
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> cancelAgreement(
        @PathVariable UUID id,
        @RequestParam String reason
    ) {
        agreementService.cancelAgreement(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Agreement cancelled"));
    }
}
```

##### b) Commission Payment Controller

**Controller**: [CommissionPaymentController.java](src/main/java/com/roms/controller/CommissionPaymentController.java)

**Endpoints**:

```java
@RestController
@RequestMapping("/api/payments")
public class CommissionPaymentController {
    
    // Record downpayment
    @PostMapping("/downpayment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> recordDownpayment(
        @RequestBody CommissionPaymentRequest request
    ) {
        PaymentDTO payment = paymentService.recordDownpayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
    
    // Record installment
    @PostMapping("/installment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> recordInstallment(
        @RequestBody CommissionPaymentRequest request
    ) {
        PaymentDTO payment = paymentService.recordInstallment(request);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
    
    // Reverse payment (SUPER_ADMIN only)
    @PostMapping("/{paymentId}/reverse")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> reversePayment(
        @PathVariable Long paymentId,
        @RequestParam String reason
    ) {
        PaymentDTO reversal = paymentService.reversePayment(paymentId, reason);
        return ResponseEntity.ok(ApiResponse.success(reversal));
    }
    
    // Get payment statement
    @GetMapping("/candidate/{candidateId}/statement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<ApiResponse> getPaymentStatement(
        @PathVariable Long candidateId
    ) {
        CommissionStatementDTO statement = 
            paymentService.getCommissionStatement(candidateId);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }
    
    // Check downpayment status (for workflow gates)
    @GetMapping("/assignment/{assignmentId}/downpayment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> checkDownpaymentStatus(
        @PathVariable Long assignmentId
    ) {
        boolean isComplete = paymentService.isDownpaymentComplete(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("isDownpaymentComplete", isComplete)
        ));
    }
    
    // Check full payment status (for workflow gates)
    @GetMapping("/assignment/{assignmentId}/fullpayment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<ApiResponse> checkFullPaymentStatus(
        @PathVariable Long assignmentId
    ) {
        boolean isComplete = paymentService.isFullPaymentComplete(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("isFullPaymentComplete", isComplete)
        ));
    }
}
```

#### 6. Frontend Implementation

##### a) Commission API Client

**File**: [commission.ts](frontend/src/api/commission.ts)

**TypeScript Interfaces**:
```typescript
export interface CommissionAgreementDTO {
  id: string;
  assignmentId: number;
  candidateId: number;
  candidateName: string;
  totalCommission: number;
  requiredDownpaymentAmount: number;
  totalPaid: number;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  signedAt?: string;
  completedAt?: string;
  cancellationReason?: string;
}

export interface CommissionPaymentRequest {
  agreementId: string;
  amount: number;
  paymentMethod: 'MPESA' | 'BANK_TRANSFER' | 'CASH';
  referenceNumber: string;
  notes?: string;
}

export interface PaymentDTO {
  id: number;
  candidateId: number;
  amount: number;
  paymentMethod: string;
  transactionType: string;
  referenceNumber: string;
  createdAt: string;
  isReversal: boolean;
}

export interface CommissionStatementDTO {
  agreement: CommissionAgreementDTO;
  payments: PaymentDTO[];
  totalPaid: number;
  outstandingBalance: number;
}
```

**API Methods**:
```typescript
// Create agreement
export const createCommissionAgreement = async (
  request: CreateCommissionAgreementRequest
): Promise<CommissionAgreementDTO> => {
  const response = await axios.post('/api/agreements', request);
  return response.data.data;
};

// Record downpayment
export const recordDownpayment = async (
  request: CommissionPaymentRequest
): Promise<PaymentDTO> => {
  const response = await axios.post('/api/payments/downpayment', request);
  return response.data.data;
};

// Record installment
export const recordInstallment = async (
  request: CommissionPaymentRequest
): Promise<PaymentDTO> => {
  const response = await axios.post('/api/payments/installment', request);
  return response.data.data;
};

// Get payment statement
export const getPaymentStatement = async (
  candidateId: number
): Promise<CommissionStatementDTO> => {
  const response = await axios.get(`/api/payments/candidate/${candidateId}/statement`);
  return response.data.data;
};

// Check downpayment status
export const checkDownpaymentStatus = async (
  assignmentId: number
): Promise<boolean> => {
  const response = await axios.get(
    `/api/payments/assignment/${assignmentId}/downpayment-status`
  );
  return response.data.data.isDownpaymentComplete;
};
```

##### b) Commission Summary Component (Applicant View)

**Component**: [CommissionSummary.tsx](frontend/src/components/CommissionSummary.tsx)

**Purpose**: Display payment status and progress to applicants

**Key Features**:
- ✅ Currency formatting (KES)
- ✅ Progress bar visualization
- ✅ Payment history table
- ✅ Workflow lock alerts

**Implementation Highlights**:
```typescript
export const CommissionSummary: React.FC<Props> = ({ candidateId }) => {
  const [statement, setStatement] = useState<CommissionStatementDTO | null>(null);
  
  useEffect(() => {
    loadStatement();
  }, [candidateId]);
  
  const loadStatement = async () => {
    const data = await getPaymentStatement(candidateId);
    setStatement(data);
  };
  
  const progressPercentage = 
    (statement.totalPaid / statement.agreement.totalCommission) * 100;
  
  return (
    <div className="commission-summary">
      {/* Progress Bar */}
      <div className="progress-bar">
        <div 
          className="progress-fill" 
          style={{ width: `${progressPercentage}%` }}
        />
      </div>
      
      {/* Financial Summary */}
      <div className="summary-grid">
        <div>Total Commission: {formatKES(statement.agreement.totalCommission)}</div>
        <div>Total Paid: {formatKES(statement.totalPaid)}</div>
        <div>Outstanding: {formatKES(statement.outstandingBalance)}</div>
      </div>
      
      {/* Payment History */}
      <table className="payment-history">
        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Method</th>
            <th>Reference</th>
          </tr>
        </thead>
        <tbody>
          {statement.payments.map(payment => (
            <tr key={payment.id}>
              <td>{formatDate(payment.createdAt)}</td>
              <td>{payment.transactionType}</td>
              <td className={payment.isReversal ? 'text-red-600' : ''}>
                {formatKES(payment.amount)}
              </td>
              <td>{payment.paymentMethod}</td>
              <td>{payment.referenceNumber}</td>
            </tr>
          ))}
        </tbody>
      </table>
      
      {/* Workflow Alerts */}
      {statement.outstandingBalance > 0 && (
        <div className="alert alert-warning">
          ⚠️ Outstanding balance must be paid before final placement
        </div>
      )}
    </div>
  );
};
```

##### c) Commission Management Component (Staff View)

**Component**: [CommissionManagement.tsx](frontend/src/components/CommissionManagement.tsx)

**Purpose**: Staff interface for recording payments

**Key Features**:
- ✅ Tabbed interface (Downpayment, Installment, Statement)
- ✅ Form validation
- ✅ M-PESA reference number capture
- ✅ Real-time balance updates

**Implementation Highlights**:
```typescript
export const CommissionManagement: React.FC<Props> = ({ candidateId, assignmentId }) => {
  const [activeTab, setActiveTab] = useState<'downpayment' | 'installment' | 'statement'>('downpayment');
  const [paymentForm, setPaymentForm] = useState<CommissionPaymentRequest>({
    agreementId: '',
    amount: 0,
    paymentMethod: 'MPESA',
    referenceNumber: '',
    notes: ''
  });
  
  const handleRecordDownpayment = async () => {
    try {
      await recordDownpayment(paymentForm);
      showSuccess('Downpayment recorded successfully');
      loadStatement(); // Refresh
    } catch (error) {
      showError('Failed to record downpayment');
    }
  };
  
  return (
    <div className="commission-management">
      {/* Tab Navigation */}
      <div className="tabs">
        <button onClick={() => setActiveTab('downpayment')}>Downpayment</button>
        <button onClick={() => setActiveTab('installment')}>Installment</button>
        <button onClick={() => setActiveTab('statement')}>Statement</button>
      </div>
      
      {/* Downpayment Form */}
      {activeTab === 'downpayment' && (
        <form onSubmit={handleRecordDownpayment}>
          <input
            type="number"
            placeholder="Amount (KES)"
            value={paymentForm.amount}
            onChange={e => setPaymentForm({...paymentForm, amount: Number(e.target.value)})}
            min={agreement.requiredDownpaymentAmount}
          />
          <select
            value={paymentForm.paymentMethod}
            onChange={e => setPaymentForm({...paymentForm, paymentMethod: e.target.value})}
          >
            <option value="MPESA">M-PESA</option>
            <option value="BANK_TRANSFER">Bank Transfer</option>
            <option value="CASH">Cash</option>
          </select>
          <input
            type="text"
            placeholder="M-PESA Code / Reference"
            value={paymentForm.referenceNumber}
            onChange={e => setPaymentForm({...paymentForm, referenceNumber: e.target.value})}
            required
          />
          <button type="submit">Record Downpayment</button>
        </form>
      )}
      
      {/* Installment Form (similar structure) */}
      {/* Statement Tab (uses CommissionSummary component) */}
    </div>
  );
};
```

##### d) Workflow Lock Banner Component

**Component**: [WorkflowLockBanner.tsx](frontend/src/components/WorkflowLockBanner.tsx)

**Purpose**: Visual indicator when workflow transitions are blocked by payment gates

**Implementation**:
```typescript
export const WorkflowLockBanner: React.FC<Props> = ({ assignmentId }) => {
  const [downpaymentComplete, setDownpaymentComplete] = useState(false);
  const [fullPaymentComplete, setFullPaymentComplete] = useState(false);
  
  useEffect(() => {
    checkPaymentStatus();
  }, [assignmentId]);
  
  const checkPaymentStatus = async () => {
    const dpStatus = await checkDownpaymentStatus(assignmentId);
    const fpStatus = await checkFullPaymentStatus(assignmentId);
    setDownpaymentComplete(dpStatus);
    setFullPaymentComplete(fpStatus);
  };
  
  return (
    <div className="workflow-locks">
      {/* Visa Processing Lock */}
      {!downpaymentComplete && (
        <div className="alert alert-danger animate-pulse">
          🔒 VISA PROCESSING LOCKED: Downpayment required
        </div>
      )}
      
      {/* Placement Lock */}
      {downpaymentComplete && !fullPaymentComplete && (
        <div className="alert alert-warning">
          🔒 FINAL PLACEMENT LOCKED: Full payment required
        </div>
      )}
      
      {/* All Clear */}
      {fullPaymentComplete && (
        <div className="alert alert-success">
          ✅ All payments complete - Ready for placement
        </div>
      )}
    </div>
  );
};
```

##### e) Page Integration

**CandidateProfilePage Integration**:
```typescript
// frontend/src/pages/CandidateProfilePage.tsx
import { WorkflowLockBanner } from '../components/WorkflowLockBanner';
import { CommissionManagement } from '../components/CommissionManagement';

export const CandidateProfilePage: React.FC = () => {
  const { id } = useParams();
  const [candidate, setCandidate] = useState<Candidate | null>(null);
  const [activeAssignment, setActiveAssignment] = useState<Assignment | null>(null);
  
  return (
    <div className="candidate-profile">
      {/* Existing candidate info */}
      
      {/* Phase 2B: Workflow Lock Banner */}
      {activeAssignment && (
        <WorkflowLockBanner assignmentId={activeAssignment.id} />
      )}
      
      {/* Phase 2B: Commission Management (Staff View) */}
      {hasRole(['OPERATIONS_STAFF', 'FINANCE_MANAGER', 'SUPER_ADMIN']) && (
        <CommissionManagement 
          candidateId={candidate.id} 
          assignmentId={activeAssignment.id} 
        />
      )}
      
      {/* Existing sections: documents, status changes, etc. */}
    </div>
  );
};
```

**MyApplicationPage Integration**:
```typescript
// frontend/src/pages/MyApplicationPage.tsx
import { CommissionSummary } from '../components/CommissionSummary';

export const MyApplicationPage: React.FC = () => {
  const { user } = useAuth();
  const [myApplication, setMyApplication] = useState<Candidate | null>(null);
  
  return (
    <div className="my-application">
      {/* Existing application info */}
      
      {/* Phase 2B: Commission Summary (Applicant View) */}
      {myApplication && (
        <section className="commission-section">
          <h2>Commission Payment Status</h2>
          <CommissionSummary candidateId={myApplication.id} />
        </section>
      )}
      
      {/* Existing sections: job details, documents, etc. */}
    </div>
  );
};
```

### Phase 2B Testing Checklist

#### Backend Tests
- [ ] Create commission agreement
- [ ] Sign agreement (verify amounts locked)
- [ ] Record downpayment (validate minimum amount)
- [ ] Record installment (validate downpayment completed first)
- [ ] Reverse payment (verify immutable ledger)
- [ ] Check downpayment status endpoint
- [ ] Check full payment status endpoint
- [ ] Test workflow gates:
  - [ ] OFFER_SIGNED → VISA_PROCESSING blocked without downpayment
  - [ ] DEPLOYED → PLACED blocked without full payment
- [ ] Test RBAC on payment endpoints
- [ ] Test SUPER_ADMIN-only payment reversal

#### Frontend Tests
- [ ] CommissionSummary displays correctly for applicants
- [ ] Currency formatting (KES) works
- [ ] Progress bar shows accurate percentage
- [ ] Payment history table populates
- [ ] WorkflowLockBanner shows red alert when downpayment incomplete
- [ ] WorkflowLockBanner shows orange alert when balance outstanding
- [ ] WorkflowLockBanner shows green when fully paid
- [ ] CommissionManagement form validation works
- [ ] Downpayment recording updates UI in real-time
- [ ] Installment recording updates UI in real-time
- [ ] M-PESA reference number captured correctly

---

## Database Schema

### Core Tables

#### users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- SUPER_ADMIN, FINANCE_MANAGER, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### candidates
```sql
CREATE TABLE candidates (
    id BIGSERIAL PRIMARY KEY,
    internal_ref_number VARCHAR(20) UNIQUE NOT NULL, -- ROMS-2026-0001
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    nationality VARCHAR(50) NOT NULL,
    passport_number VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL, -- CandidateStatus enum
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Unique passport for active (non-deleted) candidates only
CREATE UNIQUE INDEX idx_candidate_passport_active 
ON candidates(passport_number) 
WHERE deleted = false;
```

#### job_orders
```sql
CREATE TABLE job_orders (
    id BIGSERIAL PRIMARY KEY,
    employer_id BIGINT REFERENCES employers(id),
    position_title VARCHAR(100) NOT NULL,
    job_description TEXT,
    requirements TEXT,
    location VARCHAR(100), -- e.g., "Riyadh, Saudi Arabia"
    headcount INTEGER NOT NULL, -- Total positions
    status VARCHAR(20) NOT NULL, -- OPEN, FILLED, CLOSED, CANCELLED
    posted_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### assignments (Phase 2A)
```sql
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT REFERENCES candidates(id) NOT NULL,
    job_order_id BIGINT REFERENCES job_orders(id) NOT NULL,
    status VARCHAR(20) NOT NULL, -- ASSIGNED, OFFERED, PLACED, CANCELLED
    assigned_date DATE DEFAULT CURRENT_DATE,
    placed_date DATE,
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ensure one active assignment per candidate
CREATE UNIQUE INDEX idx_assignment_candidate_active 
ON assignments(candidate_id) 
WHERE status NOT IN ('CANCELLED', 'COMPLETED');
```

#### agency_commission_agreements (Phase 2B)
```sql
CREATE TABLE agency_commission_agreements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id BIGINT REFERENCES assignments(id) UNIQUE NOT NULL,
    candidate_id BIGINT REFERENCES candidates(id) NOT NULL,
    total_commission DECIMAL(10,2) NOT NULL, -- e.g., 200000.00 KES
    required_downpayment_amount DECIMAL(10,2) NOT NULL, -- e.g., 50000.00 KES
    total_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL, -- ACTIVE, COMPLETED, CANCELLED
    signed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### payments (Phase 2A & 2B)
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT REFERENCES candidates(id),
    assignment_id BIGINT REFERENCES assignments(id),
    agreement_id UUID REFERENCES agency_commission_agreements(id),
    amount DECIMAL(10,2) NOT NULL, -- Can be negative for reversals
    payment_method VARCHAR(20) NOT NULL, -- MPESA, BANK_TRANSFER, CASH
    transaction_type VARCHAR(50) NOT NULL, -- TransactionType enum
    reference_number VARCHAR(100), -- M-PESA code, bank ref, etc.
    is_reversal BOOLEAN DEFAULT FALSE,
    linked_transaction_id BIGINT REFERENCES payments(id), -- For reversals
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_candidate ON payments(candidate_id);
CREATE INDEX idx_payment_assignment ON payments(assignment_id);
CREATE INDEX idx_payment_agreement ON payments(agreement_id);
```

#### candidate_documents
```sql
CREATE TABLE candidate_documents (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT REFERENCES candidates(id) NOT NULL,
    document_type VARCHAR(20) NOT NULL, -- PASSPORT, MEDICAL, VISA, etc.
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE
);
```

### Audit Tables (Hibernate Envers)

All main tables have corresponding `_aud` tables:
- `users_aud`
- `candidates_aud`
- `assignments_aud`
- `agency_commission_agreements_aud`
- `payments_aud`

**Structure**:
```sql
CREATE TABLE candidates_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL, -- Revision number
    revtype SMALLINT, -- 0=INSERT, 1=UPDATE, 2=DELETE
    -- All fields from candidates table
    full_name VARCHAR(100),
    status VARCHAR(50),
    -- etc.
    PRIMARY KEY (id, rev)
);

CREATE TABLE revinfo (
    rev INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    revtstmp BIGINT -- Timestamp of revision
);
```

---

## API Documentation

### Authentication Endpoints

#### POST /api/auth/register
**Purpose**: Register new user  
**Access**: Public  
**Request**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "role": "APPLICANT"
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "APPLICANT"
  }
}
```

#### POST /api/auth/login
**Purpose**: Authenticate user  
**Access**: Public  
**Request**:
```json
{
  "username": "john_doe",
  "password": "SecurePass123"
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpc2lzYXJlZnJlc2h0...",
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "role": "APPLICANT"
    }
  }
}
```

### Candidate Endpoints

#### GET /api/candidates
**Purpose**: List all candidates  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF  
**Query Params**: `?status=APPLIED&page=0&size=20`  
**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "internalRefNumber": "ROMS-2026-0001",
        "fullName": "John Doe",
        "status": "APPLIED",
        "email": "john@example.com"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### PUT /api/candidates/{id}/status
**Purpose**: Update candidate status  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF  
**Request**:
```json
{
  "status": "INTERVIEW_SCHEDULED",
  "assignmentId": 5
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "INTERVIEW_SCHEDULED",
    "updatedAt": "2026-01-29T10:30:00Z"
  }
}
```

### Assignment Endpoints (Phase 2A)

#### POST /api/assignments
**Purpose**: Create new assignment  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF  
**Request**:
```json
{
  "candidateId": 1,
  "jobOrderId": 5
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": 10,
    "candidateId": 1,
    "jobOrderId": 5,
    "status": "ASSIGNED",
    "assignedDate": "2026-01-29"
  }
}
```

### Commission Agreement Endpoints (Phase 2B)

#### POST /api/agreements
**Purpose**: Create commission agreement  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF  
**Request**:
```json
{
  "assignmentId": 10,
  "totalCommission": 200000.00,
  "requiredDownpaymentAmount": 50000.00
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "assignmentId": 10,
    "candidateId": 1,
    "totalCommission": 200000.00,
    "requiredDownpaymentAmount": 50000.00,
    "totalPaid": 0.00,
    "status": "ACTIVE"
  }
}
```

#### PUT /api/agreements/{id}/sign
**Purpose**: Sign agreement (locks amounts)  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF  
**Response**:
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "ACTIVE",
    "signedAt": "2026-01-29T11:00:00Z"
  }
}
```

### Commission Payment Endpoints (Phase 2B)

#### POST /api/payments/downpayment
**Purpose**: Record downpayment  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF, FINANCE_MANAGER  
**Request**:
```json
{
  "agreementId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "amount": 50000.00,
  "paymentMethod": "MPESA",
  "referenceNumber": "QBR7YHG8KL",
  "notes": "Initial downpayment"
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": 100,
    "candidateId": 1,
    "amount": 50000.00,
    "paymentMethod": "MPESA",
    "transactionType": "AGENCY_COMMISSION_DOWNPAYMENT",
    "referenceNumber": "QBR7YHG8KL",
    "createdAt": "2026-01-29T11:15:00Z"
  }
}
```

#### GET /api/payments/assignment/{assignmentId}/downpayment-status
**Purpose**: Check if downpayment complete (workflow gate)  
**Access**: SUPER_ADMIN, OPERATIONS_STAFF  
**Response**:
```json
{
  "success": true,
  "data": {
    "isDownpaymentComplete": true
  }
}
```

---

## Development Setup

### Prerequisites
- **Java 17** (LTS): https://adoptium.net/
- **Node.js 18+**: https://nodejs.org/
- **Docker Desktop**: https://www.docker.com/products/docker-desktop
- **IntelliJ IDEA** (recommended) or Eclipse with Lombok plugin
- **Maven 3.9+** (included via Maven Wrapper)

### Step 1: Clone Repository
```bash
git clone https://github.com/your-org/roms.git
cd roms
```

### Step 2: Start Database
```bash
# Start PostgreSQL and pgAdmin containers
docker-compose up -d

# Verify containers running
docker ps

# Access pgAdmin: http://localhost:5050
# Email: admin@roms.com
# Password: admin123
```

### Step 3: Configure IDE (IntelliJ IDEA)

#### Install Lombok Plugin
1. **File → Settings → Plugins**
2. Search for **"Lombok"**
3. Click **Install**
4. **Restart IDE**

#### Import Project
1. **File → Open**
2. Select `pom.xml` from project root
3. Choose **"Open as Project"**
4. Wait for Maven dependencies to download

#### Configure Java 17
1. **File → Project Structure**
2. **Project Settings → Project**
3. Set **SDK: 17** (download if needed)
4. Set **Language Level: 17**
5. Click **Apply**

#### Enable Annotation Processing
1. **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors**
2. Check ✅ **Enable annotation processing**
3. Click **Apply**

### Step 4: Build Backend
```bash
# Build project
./mvnw clean install -DskipTests

# Run Spring Boot application
./mvnw spring-boot:run

# Or run RomsApplication.java directly in IntelliJ
```

**Expected Output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.2)

2026-01-29 10:00:00 INFO  RomsApplication - Started RomsApplication in 3.5 seconds
```

**Access**: http://localhost:8080

### Step 5: Build Frontend
```bash
cd frontend

# Install dependencies
npm install

# Development mode (hot reload)
npm run dev

# Production build
npm run build

# Copy to Spring Boot static resources
xcopy /E /I /Y dist\* ..\src\main\resources\static\
```

**Frontend Dev Server**: http://localhost:5173  
**Production Access**: http://localhost:8080 (served by Spring Boot)

### Step 6: Initialize Database

**Option A: Automatic (Hibernate DDL)**
```yaml
# application.yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update # Creates tables automatically
```

**Option B: Manual SQL Script**
```bash
# Run database-schema.sql via pgAdmin or psql
psql -h localhost -p 5433 -U romsuser -d romsdb -f database-schema.sql
```

### Step 7: Create Test Data

**Create Admin User**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@roms.com",
    "password": "password123",
    "role": "SUPER_ADMIN"
  }'
```

**Login**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

### Troubleshooting

#### Lombok Errors in IDE
**Symptom**: "Cannot find symbol: getFullName()"  
**Solution**:
1. Verify Lombok plugin installed
2. Enable annotation processing in IDE settings
3. **Project → Clean** and rebuild
4. Restart IDE

#### Port Already in Use
**Symptom**: "Port 8080 already in use"  
**Solution**:
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <PID> /F

# Or change port in application.yaml
server:
  port: 8081
```

#### Database Connection Failed
**Symptom**: "Connection refused: localhost:5433"  
**Solution**:
```bash
# Check Docker containers
docker ps

# Restart containers
docker-compose down
docker-compose up -d

# Check logs
docker logs roms-postgres
```

---

## Deployment Guide

### Production Checklist

#### 1. Environment Configuration
```yaml
# application-prod.yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db-host:5432/romsdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate # NEVER use 'update' in production
  
  security:
    jwt:
      secret: ${JWT_SECRET} # 256-bit secret from environment variable
      expiration: 3600000 # 1 hour
```

#### 2. Build Production Artifacts
```bash
# Build frontend
cd frontend
npm run build

# Copy to Spring Boot static resources
xcopy /E /I /Y dist\* ..\src\main\resources\static\

# Build backend JAR
cd ..
./mvnw clean package -DskipTests

# Output: target/roms-2.1.0.jar
```

#### 3. Database Migration
```bash
# Apply schema (Flyway or manual)
flyway migrate -url=jdbc:postgresql://prod:5432/romsdb

# Or use Liquibase
liquibase update
```

#### 4. Deploy Application
```bash
# Run as service
java -jar target/roms-2.1.0.jar \
  --spring.profiles.active=prod \
  --DB_USERNAME=prod_user \
  --DB_PASSWORD=secure_password \
  --JWT_SECRET=your-256-bit-secret

# Or use Docker
docker build -t roms:2.1.0 .
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=secure_password \
  roms:2.1.0
```

#### 5. Security Hardening
- ✅ Use HTTPS (TLS certificates)
- ✅ Environment variables for secrets (never commit)
- ✅ Database connection pooling (HikariCP configured)
- ✅ Rate limiting on authentication endpoints
- ✅ CORS configuration for frontend domain
- ✅ Regular security updates for dependencies

#### 6. Monitoring & Logging
```yaml
# application-prod.yaml
logging:
  level:
    com.roms: INFO
    org.springframework.security: WARN
  file:
    name: /var/log/roms/application.log
    max-size: 10MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

## Project File Structure

```
ROMS/
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   │   ├── auth.ts
│   │   │   ├── candidates.ts
│   │   │   ├── commission.ts          # Phase 2B
│   │   │   └── jobs.ts
│   │   ├── components/
│   │   │   ├── CommissionManagement.tsx  # Phase 2B
│   │   │   ├── CommissionSummary.tsx     # Phase 2B
│   │   │   ├── WorkflowLockBanner.tsx    # Phase 2B
│   │   │   ├── Layout.tsx
│   │   │   └── StatusBadge.tsx
│   │   ├── context/
│   │   │   └── AuthContext.tsx
│   │   ├── pages/
│   │   │   ├── CandidateProfilePage.tsx
│   │   │   ├── CandidatesPage.tsx
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── LoginPage.tsx
│   │   │   └── MyApplicationPage.tsx
│   │   └── types/
│   │       └── index.ts
│   ├── package.json
│   └── vite.config.ts
│
├── src/main/
│   ├── java/com/roms/
│   │   ├── config/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── CandidateController.java
│   │   │   ├── AssignmentController.java        # Phase 2A
│   │   │   ├── CommissionAgreementController.java  # Phase 2B
│   │   │   └── CommissionPaymentController.java    # Phase 2B
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Candidate.java
│   │   │   ├── JobOrder.java
│   │   │   ├── Assignment.java                  # Phase 2A
│   │   │   ├── AgencyCommissionAgreement.java   # Phase 2B
│   │   │   ├── Payment.java
│   │   │   └── Document.java
│   │   ├── enums/
│   │   │   ├── CandidateStatus.java
│   │   │   ├── AssignmentStatus.java            # Phase 2A
│   │   │   ├── TransactionType.java             # Phase 2B
│   │   │   └── AgreementStatus.java             # Phase 2B
│   │   ├── repository/
│   │   │   ├── CandidateRepository.java
│   │   │   ├── AssignmentRepository.java        # Phase 2A
│   │   │   ├── AgencyCommissionAgreementRepository.java  # Phase 2B
│   │   │   └── PaymentRepository.java
│   │   ├── service/
│   │   │   ├── CandidateWorkflowService.java
│   │   │   ├── AssignmentService.java           # Phase 2A
│   │   │   ├── AgencyCommissionAgreementService.java  # Phase 2B
│   │   │   └── CommissionPaymentService.java     # Phase 2B
│   │   └── RomsApplication.java
│   └── resources/
│       ├── application.yaml
│       └── static/
│           └── index.html (built from frontend)
│
├── docker-compose.yml
├── pom.xml
├── README.md
├── ARCHITECTURE.md
├── PHASE_2B_SUMMARY.md                    # Phase 2B details
├── ECLIPSE_SETUP.md
├── INTELLIJ_SETUP.md
└── COMPLETE_PROJECT_DOCUMENTATION.md      # This file
```

---

## Next Steps & Future Enhancements

### Immediate Next Steps (Post Phase 2B)
1. **Testing**: End-to-end testing of Phase 2B workflow gates
2. **Git Commit**: Tag Phase 2B completion
3. **Documentation**: Update API documentation with Phase 2B endpoints
4. **User Training**: Create user guides for commission management

### Phase 3 (Potential Features)
- **Advanced Reporting**: Financial dashboards, candidate pipeline analytics
- **Email Notifications**: Payment reminders, status change alerts
- **SMS Integration**: M-PESA payment confirmations
- **Multi-Currency Support**: USD, EUR for international placements
- **Applicant Self-Service Payments**: Online payment portal
- **Commission Installment Plans**: Flexible payment schedules
- **Employer Portal**: Self-service job posting, candidate review
- **Document Expiry Alerts**: Passport, visa expiry notifications
- **Mobile App**: React Native mobile application

### Technical Debt
- Migrate to Flyway/Liquibase for database versioning
- Implement comprehensive test coverage (JUnit, Jest)
- Add Redis caching for frequently accessed data
- Set up CI/CD pipeline (GitHub Actions)
- Implement API rate limiting
- Add request/response logging with correlation IDs

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | Jan 2026 | Phase 1: Core recruitment system |
| 2.0.0 | Jan 2026 | Phase 2A: Assignment module & employer-funded commission |
| 2.1.0 | Jan 2026 | Phase 2B: Applicant-funded commission with payment gates |

---

## Support & Contact

**Project Maintainer**: ROMS Development Team  
**Documentation**: See individual .md files in project root  
**Issues**: GitHub Issues  
**License**: Proprietary

---

**END OF COMPLETE PROJECT DOCUMENTATION**
