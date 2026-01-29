# ROMS - Recruitment Operations Management System

> **Status**: Phase 2A Complete ✅ | **Version**: 2.0.0 | **Date**: January 2026

## Overview
Enterprise-grade Recruitment Operations Management System built with modern Java stack, React frontend, and containerized PostgreSQL database. Features advanced assignment tracking, audit trails, and role-based workflow management.

## 🎯 Phase 2A Complete - Assignment Module Integrated

### Core Features Implemented
✅ **Authentication & Authorization**
- JWT-based authentication with access/refresh tokens
- Role-based access control (RBAC)
- 5 user roles: SUPER_ADMIN, FINANCE_MANAGER, OPERATIONS_STAFF, APPLICANT, EMPLOYER
- Session persistence and automatic logout on token expiry
- Secure password hashing with BCrypt

✅ **Candidate Management**
- Full candidate lifecycle: APPLIED → DOCUMENTS_SUBMITTED → INTERVIEW → MEDICAL → OFFER → DEPLOYED
- Internal reference number auto-generation
- Soft delete with unique constraint management (passport validation)
- Job application workflow with position selection
- Applicant self-service dashboard

✅ **Assignment Module (Phase 2A)**
- Many-to-many candidate-job relationships via Assignment entity
- Assignment lifecycle: ASSIGNED → OFFERED → PLACED/CANCELLED
- One active assignment per candidate enforcement
- Automatic job order headcount tracking
- Role-based assignment management (SUPER_ADMIN, OPERATIONS_STAFF)
- PLACED assignment protection (only SUPER_ADMIN can cancel)
- Full audit trail with Hibernate Envers

✅ **Job Order Management**
- Job posting creation and management
- Status tracking (OPEN, FILLED, CLOSED, CANCELLED)
- Headcount tracking and fulfillment validation
- Employer association and requirements management
- 8 test job orders across multiple locations

✅ **Document Management**
- **Local file storage** for development (uploads/ directory)
- Multi-document upload (Passport, Medical, Visa, Offer, Contract, Other)
- Role-based access control (applicants upload to own records)
- File type validation and metadata tracking
- Document expiry monitoring capability

✅ **Frontend Application (React + TypeScript)**
- Modern React 18 with TypeScript
- Vite for lightning-fast development
- Tailwind CSS responsive design
- Protected routes and authentication flow
- Complete user workflows:
  - Applicant registration and job application
  - Document upload with real-time feedback
  - Candidate dashboard
  - Admin candidate management

✅ **Development Environment**
- Docker Compose for PostgreSQL 16 and pgAdmin
- Spring Boot DevTools with hot reload
- VS Code integration with auto-compile
- Local file storage (no cloud dependencies)
- One-command setup

### Bug Fixes & Improvements
✅ Fixed circular reference issues in JPA entities (Candidate ↔ JobOrder ↔ Employer)  
✅ Added @JsonIgnoreProperties for clean JSON serialization  
✅ Hibernate lazy-loading proxy handling  
✅ API response wrapper consistency across all endpoints  
✅ Document upload parameter naming fixes (docType)  
✅ Security enhancements for applicant document uploads  
✅ Session validation on page refresh  
✅ Assignment module replaces direct candidate-job FK relationship  
✅ Database uniqueness enforcement via partial indexes  
✅ Enhanced status transition validation with assignment checks  
✅ Graceful handling of duplicate active assignments  

## 🛠️ Tech Stack

### Backend
- **Java 17** (LTS) - Modern Java features
- **Spring Boot 3.2.2** - Enterprise application framework
- **PostgreSQL 16** - Dockerized relational database
- **Spring Security** - JWT-based authentication
- **Hibernate/JPA** - ORM with automatic auditing (Envers)
- **Lombok** - Reduced boilerplate code
- **Maven** - Dependency management

### Frontend
- **React 18.2.0** - Modern UI library
- **TypeScript 5.6.2** - Type-safe JavaScript
- **Vite 5.4.21** - Fast build tool with HMR
- **Axios** - HTTP client with interceptors
- **Tailwind CSS 3.4.17** - Utility-first styling
- **React Router 7.1.1** - Client-side routing

### Database & DevOps
- **PostgreSQL 16-alpine** (Docker) - Main database
- **pgAdmin 4** (Docker) - Database management UI
- **Docker Compose** - Service orchestration
- **Git** - Version control

## 📁 Project Structure
```
ROMS/
├── src/main/java/com/roms/
│   ├── config/              # Application configuration
│   │   ├── SecurityConfig.java
│   │   ├── JpaAuditConfig.java
│   │   └── DataInitializer.java
│   ├── controller/          # REST API controllers
│   │   ├── AuthController.java
│   │   ├── CandidateController.java
│   │   ├── JobOrderController.java
│   │   └── DocumentController.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── ApiResponse.java
│   │   ├── AuthResponse.java
│   │   └── LoginRequest.java
│   ├── entity/              # JPA entities
│   │   ├── Assignment.java      # NEW: Candidate-Job assignments
│   │   ├── base/BaseAuditEntity.java
│   │   ├── User.java
│   │   ├── Candidate.java
│   │   ├── JobOrder.java
│   │   ├── Employer.java
│   │   ├── CandidateDocument.java
│   │   └── Payment.java
│   ├── enums/               # Enumerations
│   │   ├── UserRole.java
│   │   ├── CandidateStatus.java
│   │   ├── JobOrderStatus.java
│   │   └── DocumentType.java
│   ├── repository/          # Data access layer
│   │   ├── CandidateRepository.java
│   │   ├── JobOrderRepository.java
│   │   └── CandidateDocumentRepository.java
│   ├── security/            # JWT and auth
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   ├── service/             # Business logic
│   │   ├── JobApplicationService.java
│   │   ├── CandidateWorkflowService.java
│   │   └── LocalFileStorageService.java
│   └── RomsApplication.java
├── frontend/
│   ├── src/
│   │   ├── api/             # API clients
│   │   │   ├── axios.ts
│   │   │   ├── auth.ts
│   │   │   ├── jobs.ts
│   │   │   └── candidates.ts
│   │   ├── components/      # Reusable components
│   │   │   ├── Layout.tsx
│   │   │   ├── ProtectedRoute.tsx
│   │   │   └── StatusBadge.tsx
│   │   ├── context/         # React context
│   │   │   └── AuthContext.tsx
│   │   ├── pages/           # Page components
│   │   │   ├── LoginPage.tsx
│   │   │   ├── ApplicantRegisterPage.tsx
│   │   │   ├── JobsPage.tsx
│   │   │   ├── CandidatesPage.tsx
│   │   │   └── MyApplicationPage.tsx
│   │   └── types/           # TypeScript types
│   │       └── index.ts
│   ├── package.json
│   └── vite.config.ts
├── uploads/                 # Local file storage
├── docker-compose.dev.yml   # Development services
├── pom.xml                  # Maven dependencies
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- **Docker Desktop** (for PostgreSQL)
- **Node.js 18+** (for frontend)
- **Java 17+** (comes with the project)

### 1. Start Database (30 seconds)
```bash
docker-compose -f docker-compose.dev.yml up -d
```

This starts:
- PostgreSQL 16 on port 5433
- pgAdmin on port 5050

### 2. Populate Test Data (1 minute)
1. Open pgAdmin: http://localhost:5050
2. Login: `admin@roms.com` / `admin`
3. Add Server:
   - Name: ROMS Database
   - Host: `postgres` | Port: `5432`
   - Database: `roms_db`
   - Username: `postgres` | Password: `JonaMia`
4. Run `insert-jobs.sql` in Query Tool

### 3. Start Backend (2 minutes)
```bash
mvnw.cmd spring-boot:run
```

Backend starts on http://localhost:8080

### 4. Start Frontend (1 minute)
```bash
cd frontend
npm install      # First time only
npm run dev
```

Frontend starts on http://localhost:3002

### 5. Test Application
- Open http://localhost:3002
- Login: `admin` / `password123`
- Navigate through jobs, candidates, and test registration

**For detailed setup, see [QUICKSTART.md](QUICKSTART.md)**

## 🔑 Test Accounts

| Role | Username | Password | Access Level |
|------|----------|----------|-------------|
| Super Admin | admin | password123 | Full system access |
| Operations | operations | password123 | Candidates & documents |
| Finance | finance | password123 | Payments & reports |

Create applicant accounts via registration page.

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT tokens
- `GET /api/auth/me` - Get current user details
- `POST /api/auth/refresh` - Refresh access token

### Job Orders
- `GET /api/job-orders` - Get all active jobs
- `POST /api/job-orders` - Create new job (Admin/Ops)
- `GET /api/job-orders/{id}` - Get job details
- `PUT /api/job-orders/{id}` - Update job

### Candidates
- `GET /api/candidates` - Get all candidates (Admin/Ops)
- `POST /api/candidates` - Create candidate (Apply for job)
- `GET /api/candidates/{id}` - Get candidate details
- `PUT /api/candidates/{id}` - Update candidate
- `DELETE /api/candidates/{id}` - Soft delete

### Documents
- `POST /api/candidates/{id}/documents` - Upload document
- `GET /api/candidates/{id}/documents` - Get candidate documents
- `GET /api/documents/{id}/download` - Download document

### Example: Login & Create Candidate
```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Response: { "accessToken": "eyJ...", "user": {...} }

# 2. Create Candidate (use token from login)
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJ..." \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "passportNo": "AB123456"
  }'
```

## 🔒 Security Features

### JWT Authentication
- **Access Token**: 24 hours validity
- **Refresh Token**: 7 days validity
- **Algorithm**: HMAC-SHA256
- **Auto-logout**: On token expiry or invalid token

### Role-Based Access Control (RBAC)
| Resource | SUPER_ADMIN | OPERATIONS_STAFF | FINANCE_MANAGER | APPLICANT | EMPLOYER |
|----------|-------------|------------------|-----------------|-----------|----------|
| Users | ✅ Full | ❌ | ❌ | ❌ | ❌ |
| Candidates | ✅ Full | ✅ Full | ✅ View | ✅ Own | ❌ |
| Job Orders | ✅ Full | ✅ Full | ❌ | ✅ View | ✅ Own |
| Documents | ✅ Full | ✅ Full | ❌ | ✅ Own | ❌ |
| Payments | ✅ Full | ❌ | ✅ Full | ❌ | ❌ |

### Password Security
- BCrypt hashing with salt
- Minimum 8 characters
- Automatic hash verification

## 🔄 Candidate Workflow

### State Transitions
```
APPLIED → DOCUMENTS_SUBMITTED → INTERVIEW → MEDICAL → OFFER → DEPLOYED
              ↓                     ↓          ↓        ↓
         REJECTED            REJECTED     FAILED   REJECTED
```

### Validation Rules
- **Document Rule**: Passport must be valid for 6+ months
- **Medical Rule**: Cannot issue offer without medical clearance
- **Fulfillment Rule**: Job order capacity validation
- **Status Rule**: Only valid state transitions allowed

## 🗄️ Database Schema

Auto-generated via JPA/Hibernate with these key entities:

### Core Tables
- `users` - Authentication and user roles
- `candidates` - Applicant registry with soft delete
- `job_orders` - Job postings and requirements
- `employers` - Employer/client information
- `candidate_documents` - Document metadata and file references
- `payments` - Financial transactions (immutable ledger)

### Audit Tables (Envers)
- `*_AUD` - Automatic audit trail for all entities
- Tracks: who changed what, when
- Full history retention

### Key Relationships
```
User ←→ Candidate (one-to-one via user_id)
Candidate ←→ JobOrder (many-to-one)
Candidate ←→ CandidateDocument (one-to-many)
JobOrder ←→ Employer (many-to-one)
Candidate ←→ Payment (one-to-many)
```

## ⚙️ Configuration

### Database (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/roms_db
    username: postgres
    password: JonaMia
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update tables
    properties:
      hibernate:
        envers:
          audit_table_suffix: _AUD  # Audit trail tables
    show-sql: true      # Log SQL queries
```

### File Upload
```yaml
file:
  upload-dir: uploads/  # Local storage directory
  max-size: 10MB       # Max file size
```

### JWT
```yaml
jwt:
  secret: [256-bit-secret]
  expiration: 86400000      # 24 hours
  refresh-expiration: 604800000  # 7 days
```

## 🐛 Troubleshooting

### Backend Won't Start
```bash
# Check PostgreSQL is running
docker ps

# Restart database
docker-compose -f docker-compose.dev.yml restart postgres

# Check logs
docker logs roms-postgres-dev
```

### Frontend Not Loading
```bash
cd frontend
rm -rf node_modules
npm install
npm run dev
```

### Database Connection Issues
1. Verify PostgreSQL is running: `docker ps`
2. Check credentials in application.yaml
3. Test connection in pgAdmin

### Port Already in Use
```bash
# Find process using port
netstat -ano | findstr :8080

# Change port in application.yaml
serveQUICKSTART.md](QUICKSTART.md)** - Detailed setup guide
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture
- **[ASSIGNMENT_MODULE_MIGRATION.md](ASSIGNMENT_MODULE_MIGRATION.md)** - Assignment module details
- **[FRONTEND_FEATURES.md](FRONTEND_FEATURES.md)** - Frontend capabilities
- **[LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md)** - Testing procedures
- **[DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)** - Production deployment guide

- **[PHASE_1_COMPLETE.md](PHASE_1_COMPLETE.md)** - Phase 1 completion summary
- **[QUICKSTART.md](QUICKSTART.md)** - Detailed setup guide
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture
- **[FRONTEND_FEATURES.md](FRONTEND_FEATURES.md)** - Frontend capabilities

## 🎯 Phase 2 Roadmap

### Planned Features
1. **Workflow Automation**
   - Email notifications on status changes
   - Automated document expiry alerts
   - Scheduled job assignments

2. **Employer Portal**
   - Job posting interface
   - Candidate viewing dashboard
   - Application tracking

3. **Advanced Reporting**
   - Candidate status reports
   - Job order analytics
   - Financial summaries
   - Export to Excel/PDF

4. **Production Deployment**
   - Cloud database migration (Azure/AWS)
   - Google Drive integration
   - CI/CD pipeline
   - Environment-specific configs
   - HTTPS and domain setup

## 🤝 Contributing

1. Create feature branch from `main`
2. Make changes and test thoroughly
3. Update documentation
4. Submit pull request

## 📄 License

Proprietary - All Rights Reserved  
© 2026 ROMS Development Team

## 👥 Support

For issues or questions:
- Check documentation in `/docs` folder
- Review erro2.0.0 | **Status**: Phase 2A Complete ✅ | **Last Updated**: January 29, 2026eview this README and QUICKSTART.md

---

**Version**: 1.0.0 | **Status**: Phase 1 Complete ✅ | **Last Updated**: January 2026
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
