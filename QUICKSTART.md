# ROMS Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Install Prerequisites
```bash
# Check Java version
java -version  # Should be 17+

# Check PostgreSQL
psql --version  # Should be 15+

# Check Maven
mvn -v  # Should be 3.6+
```

### Step 2: Setup Database
```bash
# Login to PostgreSQL
psql -U postgres

# Create database and user
CREATE DATABASE roms_db;
CREATE USER roms_user WITH ENCRYPTED PASSWORD 'roms_password';
GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;
\q
```

### Step 3: Configure Application
Edit `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/roms_db
    username: roms_user
    password: roms_password

jwt:
  secret: MySecretKeyForJWTTokenGenerationMustBe32CharactersLong
```

### Step 4: Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Application will start on: http://localhost:8080

### Step 5: Test the API

#### 1. Register a Super Admin
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@roms.com",
    "password": "admin123",
    "fullName": "System Administrator",
    "role": "SUPER_ADMIN"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Save the `accessToken` from response!**

#### 3. Create a Candidate
```bash
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "gender": "Male",
    "passportNo": "AB123456",
    "passportExpiry": "2028-12-31",
    "email": "john.doe@example.com",
    "phoneNumber": "+254712345678",
    "country": "Kenya",
    "city": "Nairobi",
    "address": "123 Main Street",
    "expectedPosition": "Software Developer",
    "education": "Bachelor of Computer Science"
  }'
```

#### 4. Get All Candidates
```bash
curl -X GET http://localhost:8080/api/candidates \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

#### 5. Transition Candidate Status
```bash
# First, get candidate ID from step 4, then:
curl -X POST http://localhost:8080/api/candidates/1/transition \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -d '{
    "status": "DOCS_SUBMITTED"
  }'
```

## 🎯 Common Workflows

### Workflow 1: Complete Candidate Journey
```bash
# 1. Create candidate (status: APPLIED)
# 2. Submit documents
POST /api/candidates/{id}/transition {"status": "DOCS_SUBMITTED"}

# 3. Conduct interview
POST /api/candidates/{id}/transition {"status": "INTERVIEWED"}

# 4. Pass medical (requires passport valid for 6+ months)
POST /api/candidates/{id}/transition {"status": "MEDICAL_PASSED"}

# 5. Issue offer (requires medical_status = PASSED)
POST /api/candidates/{id}/transition {"status": "OFFER_ISSUED"}

# 6. Accept offer
POST /api/candidates/{id}/transition {"status": "OFFER_ACCEPTED"}

# 7. Place candidate (requires job order with capacity)
POST /api/candidates/{id}/transition {"status": "PLACED"}
```

### Workflow 2: Check Transition Validity
```bash
# Check if candidate can transition to a status
curl -X GET http://localhost:8080/api/candidates/1/can-transition/MEDICAL_PASSED \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Response will show if transition is allowed and reason if blocked
```

## 🔐 User Roles & Permissions

### Creating Different User Types

#### Finance Manager
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "finance",
    "email": "finance@roms.com",
    "password": "finance123",
    "fullName": "Finance Manager",
    "role": "FINANCE_MANAGER"
  }'
```

#### Operations Staff
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "operations",
    "email": "operations@roms.com",
    "password": "operations123",
    "fullName": "Operations Staff",
    "role": "OPERATIONS_STAFF"
  }'
```

#### Employer
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "employer1",
    "email": "employer@company.com",
    "password": "employer123",
    "fullName": "ABC Company",
    "role": "EMPLOYER"
  }'
```

## 📊 Database Queries

### View Active Candidates
```sql
SELECT * FROM v_active_candidates;
```

### Check Candidate Balance
```sql
SELECT * FROM v_candidate_balances WHERE candidate_id = 1;
```

### Job Order Fulfillment
```sql
SELECT * FROM v_job_order_fulfillment;
```

### Get Candidate Payment Balance
```sql
SELECT get_candidate_balance(1);
```

## 🔧 Troubleshooting

### Issue: "Port 8080 already in use"
**Solution**: Change port in application.yaml
```yaml
server:
  port: 8081
```

### Issue: "Could not connect to database"
**Solution**: Verify PostgreSQL is running
```bash
# Windows
services.msc  # Look for PostgreSQL service

# Linux/Mac
sudo systemctl status postgresql
```

### Issue: "JWT token expired"
**Solution**: Login again to get a new token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Issue: "Workflow transition blocked"
**Solution**: Check transition requirements
```bash
# Check why transition is blocked
curl -X GET http://localhost:8080/api/candidates/{id}/can-transition/{status} \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📝 Next Steps

1. **Phase 2 Features** (Weeks 7-10):
   - Implement payment service with reversal logic
   - Add employer and job order management
   - Create offer letter generation
   - Build financial reports

2. **Phase 3 Features** (Weeks 11-14):
   - Integrate M-Pesa API
   - Add email/SMS notifications
   - Implement document upload to S3
   - Create presigned URL generation
   - Add export to Google Sheets

## 🎓 Learning Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security with JWT](https://spring.io/guides/topicals/spring-security-architecture)
- [Hibernate Envers](https://hibernate.org/orm/envers/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 🐛 Known Issues

1. No file upload implementation yet (Phase 2)
2. No email notifications yet (Phase 3)
3. No frontend application yet

## 💡 Tips

1. Always use `Bearer` prefix with JWT token
2. Tokens expire after 24 hours - login again
3. Use soft delete - data is never truly deleted
4. Check audit tables (suffix `_AUD`) for history
5. Payment records are immutable - use reversals

---
**Happy Coding! 🚀**
