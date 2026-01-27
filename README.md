# ROMS - Recruitment Operations Management System

## Overview
Enterprise-grade Recruitment Operations Management System built with Java 17, Spring Boot 3.x, and PostgreSQL.

## Phase 1 Implementation (Core Operations)

### Completed Features
✅ **Identity & Access Management (JWT/RBAC)**
- JWT-based authentication
- Role-based access control (RBAC)
- 5 user roles: SUPER_ADMIN, FINANCE_MANAGER, OPERATIONS_STAFF, APPLICANT, EMPLOYER

✅ **Candidate Registry**
- Complete candidate lifecycle management
- Soft delete with unique constraint management
- Internal reference number generation

✅ **Workflow State Machine**
- Canonical state flow with guard logic
- Medical Rule: Cannot issue offer without medical clearance
- Document Rule: Passport must be valid for 6+ months
- Fulfillment Rule: Job order capacity validation

✅ **Basic Audit Logging**
- Hibernate Envers integration
- Automatic audit trail (_AUD tables)
- Created/Modified by and timestamp tracking

✅ **Domain Entities**
- User, Candidate, JobOrder, Payment, CandidateDocument, Employer
- Financial immutability pattern (Payment ledger)
- Document storage abstraction (Google Drive Phase 1, S3/GCS Phase 2+)

✅ **Document Management API** (Phase 1.5)
- Secure file upload/download with multipart support
- Backend file streaming (no direct cloud URLs exposed)
- Role-based document access control
- DocumentType validation (PASSPORT, MEDICAL, OFFER, etc.)

✅ **Expiry Intelligence** (Phase 1.5)
- Automated passport and medical expiry monitoring
- Scheduled job runs daily at 2:00 AM
- 90-day advance warning system (EXPIRING_SOON flag)
- Proactive alerts for expired documents (EXPIRED flag)

✅ **Offer Letter Domain** (Phase 1.5)
- Complete offer lifecycle: DRAFT → ISSUED → SIGNED
- Medical clearance guard (cannot issue without PASSED status)
- Interview optional (workflow flexibility)
- APPLICANT-only signing (legal validity)
- No concurrent offers to same candidate

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.2.2
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15+
- **ORM**: Hibernate 6.4.1 + Envers
- **Build Tool**: Maven 3.9+
- **Document Storage**: Google Drive API v3 (Phase 1), S3-compatible abstraction (Phase 2)

## Project Structure
```
src/main/java/com/roms/
├── config/              # Configuration classes
│   ├── JpaAuditConfig.java
│   └── SecurityConfig.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   └── CandidateController.java
├── dto/                 # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   └── ApiResponse.java
├── entity/              # JPA entities
│   ├── base/
│   │   └── BaseAuditEntity.java
│   ├── User.java
│   ├── Candidate.java
│   ├── JobOrder.java
│   ├── Payment.java
│   ├── CandidateDocument.java
│   └── Employer.java
├── enums/               # Enumerations
│   ├── UserRole.java
│   ├── CandidateStatus.java
│   ├── PaymentType.java
│   ├── DocumentType.java
│   ├── MedicalStatus.java
│   └── JobOrderStatus.java
├── exception/           # Custom exceptions
│   ├── WorkflowException.java
│   └── ResourceNotFoundException.java
├── repository/          # Spring Data repositories
│   ├── UserRepository.java
│   ├── CandidateRepository.java
│   ├── JobOrderRepository.java
│   ├── PaymentRepository.java
│   ├── CandidateDocumentRepository.java
│   └── EmployerRepository.java
├── security/            # Security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/             # Business logic services
│   ├── CandidateWorkflowService.java
│   └── GoogleDriveService.java
└── RomsApplication.java # Main application
```

## Setup Instructions

### Prerequisites
- Java 17 or higher
- PostgreSQL 15+
- Maven 3.6+
- Google Cloud Account (for Google Drive API - Free Tier)

### Database Setup
1. Create PostgreSQL database:
```sql
CREATE DATABASE roms_db;
CREATE USER roms_user WITH ENCRYPTED PASSWORD 'roms_password';
GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;
```

2. The application will auto-create tables on first run (ddl-auto: update)

### Application Configuration
Update `src/main/resources/application.yaml`:

```yaml
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/roms_db
spring.datasource.username=roms_user
spring.datasource.password=your_password

# JWT Secret (MUST change in production)
jwt.secret=your-256-bit-secret-key-minimum-32-characters

# Google Drive API
google.drive.credentials-file-path=credentials.json
google.drive.folder-id=your-google-drive-folder-id
```

### Google Drive Setup
1. Create a Google Cloud Project
2. Enable Google Drive API
3. Create Service Account and download credentials.json
4. Place credentials.json in project root
5. Create a folder in Google Drive and share with service account email
6. Copy folder ID from URL and update in application.yaml

**For detailed setup, see [LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md)**

### Build & Run
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run the JAR
java -jar target/Roms-0.0.1-SNAPSHOT.jar
```

Application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user details

### Candidates (RBAC protected)
- `GET /api/candidates` - Get all active candidates
- `GET /api/candidates/{id}` - Get candidate by ID
- `POST /api/candidates` - Create new candidate
- `PUT /api/candidates/{id}` - Update candidate
- `DELETE /api/candidates/{id}` - Soft delete candidate
- `POST /api/candidates/{id}/transition` - Transition candidate status
- `GET /api/candidates/{id}/can-transition/{status}` - Check if transition is allowed

### Example API Calls

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@roms.com",
    "password": "password123",
    "fullName": "System Administrator",
    "role": "SUPER_ADMIN"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

#### Create Candidate (with JWT token)
```bash
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "gender": "Male",
    "passportNo": "AB123456",
    "passportExpiry": "2028-12-31",
    "email": "john.doe@example.com",
    "phoneNumber": "+254712345678",
    "country": "Kenya"
  }'
```

## Security Features

### JWT Authentication
- Access token validity: 24 hours
- Refresh token validity: 7 days
- HMAC-SHA256 signing algorithm

### RBAC Matrix
| Endpoint | SUPER_ADMIN | FINANCE_MANAGER | OPERATIONS_STAFF | EMPLOYER | APPLICANT |
|----------|-------------|-----------------|------------------|----------|-----------|
| User Management | ✅ | ❌ | ❌ | ❌ | ❌ |
| Candidates | ✅ | ✅ | ✅ | ❌ | ❌ |
| Documents | ✅ | ❌ | ✅ | ❌ | ❌ |
| Payments | ✅ | ✅ | ❌ | ❌ | ❌ |
| Job Orders | ✅ | ❌ | ✅ | ✅ | ❌ |

## Workflow State Machine

### Canonical Flow
```
APPLIED → DOCS_SUBMITTED → INTERVIEWED → MEDICAL_PASSED → OFFER_ISSUED → OFFER_ACCEPTED → PLACED
```

### Guard Logic Rules
1. **Medical Rule**: Cannot transition to `OFFER_ISSUED` unless `medical_status == PASSED`
2. **Document Rule**: Cannot transition to `MEDICAL_PASSED` unless passport is valid for 6+ months
3. **Fulfillment Rule**: Cannot transition to `PLACED` if job order is already filled

## Data Integrity Patterns

### Soft Delete
- Uses `deleted_at` timestamp
- Partial index on passport_no: `WHERE deleted_at IS NULL`
- Allows re-registration after deletion

### Financial Immutability
- Payment records are NEVER deleted or edited
- Reversal pattern: Create negative transaction linked to original
- All monetary values use `BigDecimal` with `HALF_UP` rounding

### Audit Trail
- Every entity extends `BaseAuditEntity`
- Automatic tracking: `created_by`, `created_at`, `last_modified_by`, `last_modified_at`
- Hibernate Envers creates `_AUD` tables automatically

## Next Steps (Phase 2 & 3)

### Phase 2: Financials & Employers (Weeks 7-10)
- [ ] Payment service with reversal logic
- [ ] Employer management
- [ ] Job order fulfillment tracking
- [ ] Financial reporting
- [ ] PDF offer letter generation with template engine
- [ ] Email/SMS notifications for offer issuance

### Phase 3: Automation (Weeks 11-14)
- [ ] M-Pesa API integration
- [ ] S3/GCS abstraction layer (multi-cloud document storage)
- [ ] Presigned URL generation for secure downloads
- [ ] Google Sheets/Docs export
- [ ] DocuSign/Adobe Sign integration for digital signatures
- [ ] Dashboard analytics and reporting

## API Surface (Complete Reference)

### Authentication Endpoints
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Authenticate user |
| POST | `/api/auth/refresh` | ❌ | Refresh access token |

### Candidate Management
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | `/api/candidates` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF, FINANCE_MANAGER | List all candidates (paginated) |
| POST | `/api/candidates` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | Create new candidate |
| GET | `/api/candidates/{id}` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF, FINANCE_MANAGER | Get candidate by ID |
| PUT | `/api/candidates/{id}` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | Update candidate |
| DELETE | `/api/candidates/{id}` | ✅ | SUPER_ADMIN | Soft delete candidate |
| GET | `/api/candidates/search` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | Search candidates |

### Workflow Transitions
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/candidates/{id}/shortlist` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | APPLIED → SHORTLISTED |
| POST | `/api/candidates/{id}/schedule-interview` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | SHORTLISTED → INTERVIEW_SCHEDULED |
| POST | `/api/candidates/{id}/select` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | INTERVIEW_SCHEDULED → SELECTED |
| POST | `/api/candidates/{id}/process-medical` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | SELECTED → MEDICAL_IN_PROGRESS |
| POST | `/api/candidates/{id}/mark-medical-fit` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | MEDICAL_IN_PROGRESS → MEDICAL_CLEARED |
| POST | `/api/candidates/{id}/deploy` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | MEDICAL_CLEARED → DEPLOYED |
| POST | `/api/candidates/{id}/place` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | DEPLOYED → PLACED |

### Document Management (Provider-Agnostic: Google Drive/S3/GCS)
| Method | Endpoint | Auth | Role | Status | Description |
|--------|----------|------|------|--------|-------------|
| POST | `/api/candidates/{id}/documents` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Upload document (multipart/form-data) |
| GET | `/api/candidates/{id}/documents` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF, APPLICANT | ✅ | List candidate documents |
| GET | `/api/documents/{documentId}/download` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF, APPLICANT | ✅ | Stream document (backend proxy, no direct URLs) |
| DELETE | `/api/documents/{documentId}` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Soft delete document from storage |
| GET | `/api/documents/{documentId}/share` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Generate backend download link |

### Offer Letter Management (Phase 1.5 - COMPLETE)
| Method | Endpoint | Auth | Role | Status | Description |
|--------|----------|------|------|--------|-------------|
| POST | `/api/offers/draft` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Create draft offer letter |
| POST | `/api/offers/{id}/issue` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Issue offer (REQUIRES medical clearance) |
| POST | `/api/offers/{id}/sign` | ✅ | APPLICANT | ✅ | Sign offer letter (own offers only) |
| POST | `/api/offers/{id}/withdraw` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Withdraw offer with reason |
| GET | `/api/offers/candidate/{id}` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF, APPLICANT | ✅ | Get all candidate offers |
| GET | `/api/offers/job-order/{id}` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF, EMPLOYER | ✅ | Get all job order offers |
| GET | `/api/offers/candidate/{id}/can-receive` | ✅ | SUPER_ADMIN, OPERATIONS_STAFF | ✅ | Check if candidate can receive new offer |

### Payment Management (Phase 2)
| Method | Endpoint | Auth | Role | Status | Description |
|--------|----------|------|------|--------|-------------|
| POST | `/api/payments` | ✅ | SUPER_ADMIN, FINANCE_MANAGER | ⏳ | Record payment |
| GET | `/api/payments/candidate/{id}` | ✅ | SUPER_ADMIN, FINANCE_MANAGER | ⏳ | Get candidate payment history |
| GET | `/api/payments/balance/{id}` | ✅ | SUPER_ADMIN, FINANCE_MANAGER | ⏳ | Get candidate balance |

**Legend:** ✅ Implemented | 🔄 Backend Ready (Frontend Pending) | ⏳ Planned

## Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## Troubleshooting

### Common Issues
1. **Port already in use**: Change server port in application.yaml
2. **Database connection failed**: Verify PostgreSQL is running and credentials are correct
3. **JWT secret too short**: Ensure secret is at least 32 characters

## Contributing
This is an enterprise project. Follow the blueprint specifications strictly.

## License
Proprietary - All rights reserved

---
**ROMS Version 1.5** - Phase 1 + Phase 1.5 Complete ✅

**Phase 1.5 Deliverables:**
- ✅ Document Management API (secure upload/download)
- ✅ Expiry Intelligence (automated monitoring)
- ✅ Offer Letter Domain (complete lifecycle with business rules)

See [PHASE_1_5_IMPLEMENTATION.md](PHASE_1_5_IMPLEMENTATION.md) for detailed implementation notes.
