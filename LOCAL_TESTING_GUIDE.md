# ROMS Local Testing Guide

This guide will help you set up and test the ROMS application locally on your machine.

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **PostgreSQL 15+** (or Docker for containerized database)
- **Google Drive API credentials** (JSON file)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code

---

## Step 1: Database Setup

### Option A: Using Docker (Recommended)

1. **Start PostgreSQL with Docker Compose:**
```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port `5432`
- pgAdmin on port `5050` (http://localhost:5050)

2. **Access pgAdmin:**
- URL: http://localhost:5050
- Email: `admin@roms.com`
- Password: `admin`

3. **Connect to Database in pgAdmin:**
- Right-click "Servers" → "Create" → "Server"
- Name: `ROMS-Local`
- Host: `postgres` (Docker network) or `localhost`
- Port: `5432`
- Username: `roms_user`
- Password: `roms_password`
- Database: `roms_db`

### Option B: Local PostgreSQL Installation

1. **Install PostgreSQL** from https://www.postgresql.org/download/

2. **Create Database:**
```sql
CREATE DATABASE roms_db;
CREATE USER roms_user WITH ENCRYPTED PASSWORD 'roms_password';
GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;
```

---

## Step 2: Google Drive API Setup

### 1. Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select existing)
3. Enable **Google Drive API**:
   - Navigate to "APIs & Services" → "Library"
   - Search for "Google Drive API"
   - Click "Enable"

### 2. Create Service Account

1. Go to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "Service Account"
3. Fill in details:
   - Name: `ROMS Document Manager`
   - Role: `Editor` (or custom role with Drive permissions)
4. Click "Done"

### 3. Generate JSON Key

1. Click on the created service account
2. Go to "Keys" tab
3. Click "Add Key" → "Create new key"
4. Select "JSON" format
5. Download the JSON file

### 4. Setup Google Drive Folder

1. Create a folder in your Google Drive: "ROMS-Documents"
2. Right-click folder → "Share"
3. Add the service account email (from JSON file: `client_email`)
4. Give "Editor" permission
5. Copy the Folder ID from URL:
   - URL: `https://drive.google.com/drive/folders/1ABC...XYZ`
   - Folder ID: `1ABC...XYZ`

### 5. Configure Application

1. Copy the downloaded JSON file to project root:
```bash
cp ~/Downloads/your-service-account-key.json credentials.json
```

2. Update `application.yaml` or set environment variables:
```yaml
google:
  drive:
    credentials-file-path: credentials.json
    folder-id: YOUR_FOLDER_ID_HERE
```

Or use environment variables:
```bash
export GOOGLE_CREDENTIALS_PATH=/path/to/credentials.json
export GOOGLE_DRIVE_FOLDER_ID=1ABC...XYZ
```

---

## Step 3: Build and Run Application

### 1. Clean and Build

```bash
# Clean previous builds
mvn clean

# Build project (skip tests for now)
mvn package -DskipTests

# Or use the wrapper scripts
./mvnw clean package -DskipTests    # Linux/Mac
mvnw.cmd clean package -DskipTests  # Windows
```

### 2. Run Application

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using wrapper
./mvnw spring-boot:run    # Linux/Mac
mvnw.cmd spring-boot:run  # Windows

# Option 3: Run JAR directly
java -jar target/Roms-0.0.1-SNAPSHOT.jar
```

### 3. Verify Startup

Check console output for:
```
✅ Started RomsApplication in X.XXX seconds
✅ Tomcat started on port(s): 8080
✅ Google Drive Service initialized successfully
```

---

## Step 4: Test Authentication APIs

### 1. Register First User (SUPER_ADMIN)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@roms.com",
    "password": "Admin@123",
    "role": "SUPER_ADMIN"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@roms.com",
      "role": "SUPER_ADMIN"
    },
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

**Save the accessToken for subsequent requests!**

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin@123"
  }'
```

### 3. Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

---

## Step 5: Test Candidate Workflow

### 1. Create Employer (as SUPER_ADMIN)

```bash
# First, get your access token from login/register
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# Create employer record directly in database or via API (if you implement EmployerController)
# For now, insert via SQL:
```

**SQL Insert:**
```sql
INSERT INTO employers (company_name, contact_person, email, phone, industry, country, is_active, created_by, created_at)
VALUES ('ABC Company', 'John Doe', 'john@abc.com', '+1234567890', 'Manufacturing', 'USA', true, 'admin', NOW());

INSERT INTO job_orders (job_order_ref, employer_id, job_title, headcount_required, headcount_filled, 
                        monthly_salary, contract_duration_months, status, start_date, is_active, created_by, created_at)
VALUES ('JO-2024-001', 1, 'Factory Worker', 10, 0, 2500.00, 24, 'OPEN', '2024-03-01', true, 'admin', NOW());
```

### 2. Create Candidate

```bash
curl -X POST http://localhost:8080/api/candidates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Maria",
    "middleName": "Santos",
    "lastName": "Garcia",
    "dateOfBirth": "1995-05-15",
    "gender": "FEMALE",
    "nationality": "Filipino",
    "passportNo": "P1234567",
    "passportExpiry": "2028-12-31",
    "email": "maria.garcia@email.com",
    "phoneNumber": "+639171234567",
    "alternatePhone": "+639281234567",
    "currentAddress": "123 Main St, Manila",
    "permanentAddress": "456 Home St, Quezon City",
    "expectedPosition": "Factory Worker",
    "previousExperience": "2 years factory work",
    "educationLevel": "High School Graduate",
    "languagesSpoken": "English, Tagalog",
    "jobOrderId": 1,
    "currentStatus": "APPLIED",
    "medicalStatus": "PENDING"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Candidate created successfully",
  "data": {
    "id": 1,
    "internalRefNo": "CAND-2024-0001",
    "firstName": "Maria",
    "lastName": "Garcia",
    "passportNo": "P1234567",
    "currentStatus": "APPLIED",
    ...
  }
}
```

### 3. Get All Candidates

```bash
curl -X GET "http://localhost:8080/api/candidates?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Get Candidate by ID

```bash
curl -X GET http://localhost:8080/api/candidates/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Search Candidates

```bash
# Search by name
curl -X GET "http://localhost:8080/api/candidates/search?keyword=Maria&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Search by passport
curl -X GET "http://localhost:8080/api/candidates/search?keyword=P1234567&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Test Workflow Transitions

#### a. Shortlist Candidate
```bash
curl -X POST http://localhost:8080/api/candidates/1/shortlist \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Good candidate with relevant experience"
  }'
```

#### b. Schedule Interview
```bash
curl -X POST http://localhost:8080/api/candidates/1/schedule-interview \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Interview scheduled for tomorrow 10 AM"
  }'
```

#### c. Select Candidate
```bash
curl -X POST http://localhost:8080/api/candidates/1/select \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Passed interview successfully"
  }'
```

#### d. Process Medical
```bash
curl -X POST http://localhost:8080/api/candidates/1/process-medical \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Medical scheduled at clinic"
  }'
```

#### e. Mark Medical Fit
```bash
curl -X POST http://localhost:8080/api/candidates/1/mark-medical-fit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "All medical tests passed"
  }'
```

#### f. Deploy Candidate
```bash
curl -X POST http://localhost:8080/api/candidates/1/deploy \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Deployment date: 2024-03-15"
  }'
```

#### g. Place Candidate
```bash
curl -X POST http://localhost:8080/api/candidates/1/place \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Successfully placed at client site"
  }'
```

---

## Step 6: Test Document Upload (Google Drive)

### Using Postman or curl with multipart/form-data

**Create a document upload endpoint first (not implemented yet, but here's how to test):**

```bash
curl -X POST http://localhost:8080/api/candidates/1/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/passport-scan.pdf" \
  -F "docType=PASSPORT" \
  -F "documentNumber=P1234567" \
  -F "expiryDate=2028-12-31"
```

This will upload the file to Google Drive and store metadata in database.

---

## Step 7: Verify Database Records

### Using pgAdmin

1. Open pgAdmin (http://localhost:5050)
2. Navigate to `ROMS-Local` → `Databases` → `roms_db` → `Schemas` → `public` → `Tables`
3. Right-click on tables and select "View/Edit Data" → "All Rows"

### Check Created Records

```sql
-- Check users
SELECT * FROM users;

-- Check candidates
SELECT * FROM candidates WHERE deleted_at IS NULL;

-- Check candidate history (audit trail)
SELECT * FROM candidates_aud ORDER BY rev DESC;

-- Check candidate balance view
SELECT * FROM v_candidate_balances;

-- Check active candidates view
SELECT * FROM v_active_candidates;
```

---

## Step 8: Test Role-Based Access Control

### 1. Create Different Role Users

```bash
# FINANCE_MANAGER
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "finance",
    "email": "finance@roms.com",
    "password": "Finance@123",
    "role": "FINANCE_MANAGER"
  }'

# OPERATIONS_STAFF
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ops",
    "email": "ops@roms.com",
    "password": "Ops@123",
    "role": "OPERATIONS_STAFF"
  }'
```

### 2. Test Access Restrictions

```bash
# Login as OPERATIONS_STAFF
OPS_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "ops",
    "password": "Ops@123"
  }' | jq -r '.data.accessToken')

# Try to access candidates (should work)
curl -X GET http://localhost:8080/api/candidates \
  -H "Authorization: Bearer $OPS_TOKEN"

# Try admin-only operations (should fail with 403 Forbidden)
# Depends on your SecurityConfig rules
```

---

## Step 9: Test Error Handling

### 1. Invalid Workflow Transition
```bash
# Try to PLACE a candidate who is still in APPLIED status
curl -X POST http://localhost:8080/api/candidates/1/place \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "This should fail"
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Invalid workflow transition: Cannot move from APPLIED to PLACED",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 2. Duplicate Passport Number
```bash
# Try to create candidate with same passport (should fail)
curl -X POST http://localhost:8080/api/candidates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "Duplicate",
    "passportNo": "P1234567",
    ...
  }'
```

### 3. Invalid JWT Token
```bash
curl -X GET http://localhost:8080/api/candidates \
  -H "Authorization: Bearer INVALID_TOKEN"
```

---

## Step 10: Monitor Application Logs

### View Real-time Logs

**Console Output:**
- Watch terminal where application is running
- Look for SQL queries, security events, API requests

**Log Levels:**
```
DEBUG: com.roms - Application logic
DEBUG: org.springframework.security - Authentication/Authorization
DEBUG: org.hibernate.SQL - Database queries
TRACE: org.hibernate.type - SQL parameter values
```

### Common Log Patterns to Watch

```
✅ Good:
- "User authenticated successfully: admin"
- "Candidate workflow transition: APPLIED -> SHORTLISTED"
- "File uploaded successfully. Drive File ID: 1ABC..."

❌ Errors:
- "Invalid credentials for user: admin"
- "Invalid workflow transition"
- "Google Drive initialization failed"
```

---

## Troubleshooting

### Issue: Application won't start

**Check:**
1. PostgreSQL is running: `docker ps` or check service status
2. Database credentials in `application.yaml` match your setup
3. Port 8080 is not already in use: `netstat -an | findstr 8080` (Windows)

### Issue: Google Drive upload fails

**Check:**
1. `credentials.json` file exists and path is correct
2. Service account has access to the folder
3. Folder ID is correct in configuration
4. Drive API is enabled in Google Cloud Console

### Issue: 401 Unauthorized

**Check:**
1. Token is included in Authorization header
2. Token format: `Bearer <token>`
3. Token hasn't expired (24 hours for access token)
4. Use refresh token to get new access token

### Issue: Database errors

**Check:**
1. Database schema is created (run `database-schema.sql`)
2. User has proper permissions
3. Connection string is correct in `application.yaml`

---

## Next Steps

1. **Implement Missing Controllers:**
   - `EmployerController` for employer management
   - `JobOrderController` for job order management
   - `PaymentController` for payment tracking
   - `DocumentController` for document operations

2. **Add Validation:**
   - Passport expiry validation (minimum 6 months)
   - Medical fitness validation before deployment
   - Payment balance validation

3. **Implement Advanced Features:**
   - Email notifications for workflow transitions
   - PDF report generation
   - Dashboard with statistics
   - Bulk candidate import

4. **Testing:**
   - Write unit tests for services
   - Integration tests for controllers
   - Test all workflow edge cases

---

## Useful Commands

### Maven
```bash
# Clean build
mvn clean install

# Run tests
mvn test

# Run specific test
mvn test -Dtest=CandidateWorkflowServiceTest

# Skip tests
mvn package -DskipTests

# Generate project structure
mvn dependency:tree
```

### Docker
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### PostgreSQL
```bash
# Connect to database (if installed locally)
psql -U roms_user -d roms_db

# Export database
pg_dump -U roms_user roms_db > backup.sql

# Import database
psql -U roms_user roms_db < backup.sql
```

---

## API Documentation Summary

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/auth/register` | POST | No | Register new user |
| `/api/auth/login` | POST | No | Login user |
| `/api/auth/refresh` | POST | No | Refresh access token |
| `/api/candidates` | GET | Yes | Get all candidates (paginated) |
| `/api/candidates` | POST | Yes | Create new candidate |
| `/api/candidates/{id}` | GET | Yes | Get candidate by ID |
| `/api/candidates/{id}` | PUT | Yes | Update candidate |
| `/api/candidates/{id}` | DELETE | Yes | Soft delete candidate |
| `/api/candidates/search` | GET | Yes | Search candidates |
| `/api/candidates/{id}/shortlist` | POST | Yes | Shortlist candidate |
| `/api/candidates/{id}/schedule-interview` | POST | Yes | Schedule interview |
| `/api/candidates/{id}/select` | POST | Yes | Select candidate |
| `/api/candidates/{id}/process-medical` | POST | Yes | Process medical |
| `/api/candidates/{id}/mark-medical-fit` | POST | Yes | Mark as medically fit |
| `/api/candidates/{id}/deploy` | POST | Yes | Deploy candidate |
| `/api/candidates/{id}/place` | POST | Yes | Place candidate |

---

## Support

If you encounter issues:
1. Check application logs
2. Verify database connectivity
3. Ensure Google Drive credentials are valid
4. Review this guide's troubleshooting section
5. Check `ARCHITECTURE.md` for system design details

Good luck with testing! 🚀
