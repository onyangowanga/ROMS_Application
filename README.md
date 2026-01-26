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
- Document vault ready (S3 integration)

## Tech Stack
- **Backend**: Java 17, Spring Boot 4.0.2
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15+
- **ORM**: Hibernate + Envers
- **Build Tool**: Maven
- **Object Storage**: Google Drive API (Free Tier)

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
- [ ] Offer letter generation (PDF)
- [ ] Financial reporting

### Phase 3: Automation (Weeks 11-14)
- [ ] M-Pesa API integration
- [ ] Email/SMS notifications
- [ ] Document upload to S3
- [ ] Presigned URL generation
- [ ] Google Sheets/Docs export

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
**ROMS Version 1.1** - Phase 1 Complete ✅
