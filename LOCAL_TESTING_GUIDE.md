# ROMS Local Testing Guide

> **Phase 1 Complete** ✅ | Docker-based development environment

This guide helps you set up, test, and develop the ROMS application locally.

---

## 📋 Prerequisites

- **Docker Desktop** (Windows/Mac) or Docker Engine (Linux)
- **Java 17+** (included via Maven wrapper)
- **Node.js 18+** (for frontend development)
- **Git** (for version control)

### Quick Check
```bash
docker --version    # Should be 20.x+
node --version      # Should be v18.x+
java -version       # Should be 17.x+
git --version       # Any recent version
```

---

## 🚀 Quick Start (5 Minutes)

### 1. Start Database
```bash
cd c:\Programing\Realtime projects\ROMS\Roms\Roms
docker-compose -f docker-compose.dev.yml up -d
```

**Verify services:**
```bash
docker ps
# Should show: roms-postgres-dev, roms-pgadmin-dev
```

### 2. Start Backend
```bash
mvnw.cmd spring-boot:run
```

**Expected output:**
```
Started RomsApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### 3. Install Frontend Dependencies
```bash
cd frontend
npm install
```

### 4. Start Frontend
```bash
npm run dev
```

**Expected output:**
```
VITE v5.x.x  ready in XXX ms
➜  Local:   http://localhost:3002/
```

### 5. Access Application
- **Frontend**: http://localhost:3002
- **Backend API**: http://localhost:8080
- **pgAdmin**: http://localhost:5050

---

## 🗄️ Database Setup

### Container Details
**docker-compose.dev.yml** provides:
- **PostgreSQL 16** on port `5433` (host) → `5432` (container)
- **pgAdmin 4** on port `5050`
- **Network**: roms-network (bridge)
- **Volume**: postgres_data (persistent storage)

### Database Configuration
```yaml
Database: roms_db
Host: localhost (from host) or postgres (from Docker)
Port: 5433 (from host) or 5432 (from container)
Username: postgres
Password: JonaMia
```

### Access pgAdmin
1. Open http://localhost:5050
2. Login:
   - Email: `admin@roms.com`
   - Password: `admin`
3. Add server:
   - Name: `ROMS Dev`
   - Host: `host.docker.internal` (Windows/Mac) or `172.17.0.1` (Linux)
   - Port: `5432` (internal) or `5433` (external)
   - Username: `postgres`
   - Password: `JonaMia`
   - Database: `roms_db`

### Insert Test Data
The database schema is auto-created by JPA. Insert test job orders:

```bash
# Connect to PostgreSQL
docker exec -it roms-postgres-dev psql -U postgres -d roms_db

# Or use file
docker exec -i roms-postgres-dev psql -U postgres -d roms_db < insert-jobs.sql
```

**insert-jobs.sql** contains:
- 1 test employer
- 8 job orders (Dubai, Abu Dhabi, Riyadh, Doha)

---

## 🔧 Development Workflow

### Backend Development

#### Run with Auto-Reload
```bash
mvnw.cmd spring-boot:run
```

Spring DevTools automatically reloads changes when you recompile in your IDE.

#### Clean Build
```bash
mvnw.cmd clean install
```

#### Run Tests
```bash
mvnw.cmd test
```

#### Check Logs
Check the terminal where `mvnw.cmd spring-boot:run` is running. Errors will show there.

### Frontend Development

#### Start Dev Server
```bash
cd frontend
npm run dev
```

Vite provides:
- Hot Module Replacement (HMR) - instant updates
- Fast cold start
- TypeScript type checking

#### Build for Production
```bash
npm run build
```

Output: `frontend/dist/` folder

#### Preview Production Build
```bash
npm run preview
```

#### Type Checking
```bash
npm run type-check  # If configured
```

### Database Operations

#### View Logs
```bash
docker logs roms-postgres-dev
```

#### Connect via psql
```bash
docker exec -it roms-postgres-dev psql -U postgres -d roms_db
```

#### Common Queries
```sql
-- View all candidates
SELECT * FROM candidates WHERE deleted = false;

-- View job orders
SELECT * FROM job_orders WHERE status = 'OPEN';

-- View users
SELECT id, username, email, role FROM users;

-- View documents
SELECT * FROM candidate_documents;
```

#### Restart Database
```bash
docker-compose -f docker-compose.dev.yml restart postgres
```

#### Stop All Services
```bash
docker-compose -f docker-compose.dev.yml down
```

#### Reset Database (Delete All Data)
```bash
# WARNING: This deletes all data!
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```

---

## 🧪 Testing Guide

### Test Accounts
Created by DataInitializer.java on first run:

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin | password123 | SUPER_ADMIN | Full system access |
| operations | password123 | OPERATIONS_STAFF | Candidate & job management |
| finance | password123 | FINANCE_MANAGER | View candidates, manage payments |

### Frontend Testing Workflow

#### 1. Login as Admin
1. Go to http://localhost:3002
2. Click **"Login"**
3. Username: `admin`, Password: `password123`
4. Should redirect to Dashboard

#### 2. View Jobs
1. Click **"Jobs"** in navigation
2. Should see 8 test jobs (if insert-jobs.sql was run)
3. Each job shows: title, location, positions, salary, status

#### 3. Create Applicant
1. Logout (top-right)
2. Click **"Apply for Job"**
3. Fill form:
   - Email: test@example.com
   - Password: password123
   - First Name: John
   - Last Name: Doe
   - Passport: AB123456
   - Phone: +971501234567
   - Select job from dropdown
4. Click **"Register"**
5. Should auto-login and redirect to "My Application"

#### 4. Upload Documents (as Applicant)
1. On "My Application" page
2. Click **"Upload Document"**
3. Select document type: PASSPORT, RESUME, EDUCATION, etc.
4. Choose file (PDF, JPG, PNG, DOCX - max 10MB)
5. Click **"Upload"**
6. Document appears in list below

**File location**: `/uploads/` folder in project root

#### 5. View Candidates (as Admin)
1. Logout and login as `admin`
2. Click **"Candidates"**
3. See all registered applicants
4. Click on a candidate to view details
5. See uploaded documents with download links

### Backend API Testing

#### Using cURL

**1. Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"password123\"}"
```

**Save accessToken from response!**

**2. Get Candidates**
```bash
curl -X GET http://localhost:8080/api/candidates \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**3. Create Candidate**
```bash
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane@example.com\",\"passportNo\":\"CD789012\",\"phoneNumber\":\"+971501234567\",\"jobOrderId\":1}"
```

**4. Upload Document**
```bash
curl -X POST "http://localhost:8080/api/candidates/1/documents?docType=PASSPORT" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -F "file=@C:/path/to/passport.pdf"
```

#### Using Postman

1. Import collection (if available)
2. Set environment variables:
   - `baseUrl`: http://localhost:8080
   - `token`: (set after login)
3. Run requests in order:
   - Auth → Login
   - Candidates → Get All
   - Candidates → Create
   - Documents → Upload

---


## 🐛 Troubleshooting

### Backend Won't Start
**Symptoms**: Application fails to start, errors in console

**Solutions**:
```bash
# 1. Check if database is running
docker ps

# 2. Restart PostgreSQL
docker-compose -f docker-compose.dev.yml restart postgres

# 3. Verify credentials in application.yaml:
#    username: postgres
#    password: JonaMia
#    port: 5433

# 4. Clean and rebuild
mvnw.cmd clean install
```

### Port Already in Use
**Symptoms**: "Port 8080 already in use"

**Solutions**:
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <PID> /F

# Or change port in application.yaml
server.port: 8081
```

### Database Connection Refused
**Symptoms**: "Connection refused" or "Could not connect to database"

**Solutions**:
```bash
# Check Docker container
docker ps

# Check container logs
docker logs roms-postgres-dev

# Restart database
docker-compose -f docker-compose.dev.yml restart postgres

# Wait 10 seconds for health check, then restart backend
```

### Frontend Build Errors
**Symptoms**: npm errors, module not found

**Solutions**:
```bash
cd frontend

# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install

# Or use npm cache clean
npm cache clean --force
npm install
```

### Document Upload Fails
**Symptoms**: Upload button doesn't work, files not saving

**Check**:
1. File size < 10MB
2. File type: PDF, JPG, PNG, DOCX only
3. `uploads/` folder exists and is writable
4. Logged in as correct user (applicants upload to own records)

**Fix**:
```bash
# Create uploads folder if missing
mkdir uploads

# Check permissions (Linux/Mac)
chmod 755 uploads
```

### JWT Token Expired
**Symptoms**: 401 Unauthorized after some time

**Solution**:
- Tokens expire after 24 hours
- Logout and login again
- Frontend auto-validates token on page refresh

### Candidates Page Blank
**Symptoms**: Empty page, no errors

**Check**:
1. Backend console for errors
2. Browser console (F12) for JavaScript errors
3. Network tab (F12) for failed API calls

**If circular reference error**:
- Already fixed in Phase 1 with @JsonIgnoreProperties

### Docker Container Won't Start
**Symptoms**: `docker ps` shows no containers

**Solutions**:
```bash
# Check Docker Desktop is running (Windows/Mac)

# View all containers (including stopped)
docker ps -a

# Remove and recreate
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d

# Check logs
docker logs roms-postgres-dev
```

### pgAdmin Can't Connect
**Symptoms**: "Could not connect to server"

**Solutions**:
1. Use `host.docker.internal` (Windows/Mac) or `172.17.0.1` (Linux)
2. Port: `5432` (internal) or `5433` (external)
3. Username: `postgres`, Password: `JonaMia`
4. Ensure PostgreSQL container is running: `docker ps`

### Auto-Reload Not Working
**Symptoms**: Code changes don't apply automatically

**Backend**:
- Ensure Spring DevTools is in pom.xml
- Build project in IDE (Ctrl+F9 in IntelliJ)
- Check terminal for "Restarting due to changes"

**Frontend**:
- Vite HMR should be automatic
- Check terminal for errors
- Try hard refresh: Ctrl+Shift+R

---

## 📊 Performance Tips

### Database Optimization
```sql
-- Add indexes for frequently queried fields
CREATE INDEX idx_candidates_passport ON candidates(passport_no);
CREATE INDEX idx_candidates_status ON candidates(current_status);
CREATE INDEX idx_job_orders_employer ON job_orders(employer_id);
```

### Backend Tuning
```yaml
# application.yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20  # Batch insert optimization
        cache:
          use_second_level_cache: true  # Enable caching
```

### Frontend Optimization
```bash
# Production build with minification
npm run build

# Analyze bundle size
npm run build -- --analyze
```

---

## 📚 Additional Resources

### Documentation
- [PHASE_1_COMPLETE.md](PHASE_1_COMPLETE.md) - Phase 1 summary
- [README.md](README.md) - Main documentation
- [QUICKSTART.md](QUICKSTART.md) - Quick setup guide
- [FRONTEND_FEATURES.md](FRONTEND_FEATURES.md) - Frontend capabilities
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture

### Tools
- **pgAdmin**: http://localhost:5050 - Database management
- **Postman**: API testing (import collection if available)
- **VS Code Extensions**:
  - Spring Boot Extension Pack
  - Vite (for frontend)
  - PostgreSQL (syntax highlighting)

### Learning Resources
- [Spring Boot Docs](https://docs.spring.io/spring-boot/)
- [React Docs](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)

---

## 🎯 Testing Checklist

### Before Each Development Session
- [ ] Start Docker containers: `docker-compose -f docker-compose.dev.yml up -d`
- [ ] Verify database: `docker ps`
- [ ] Start backend: `mvnw.cmd spring-boot:run`
- [ ] Start frontend: `cd frontend && npm run dev`
- [ ] Check all services are running (ports 5433, 8080, 3002, 5050)

### After Code Changes
- [ ] Backend: IDE auto-compiles (or manually build)
- [ ] Frontend: Vite auto-reloads
- [ ] Test in browser: http://localhost:3002
- [ ] Check console for errors (browser F12 & backend terminal)

### Before Committing
- [ ] All features tested manually
- [ ] No console errors
- [ ] Backend builds successfully: `mvnw.cmd clean install`
- [ ] Frontend builds: `cd frontend && npm run build`
- [ ] Run cleanup.bat to remove temporary files
- [ ] Update documentation if needed

### End of Day
- [ ] Commit changes to Git
- [ ] Stop services: `docker-compose -f docker-compose.dev.yml down`
- [ ] Backend: Ctrl+C in terminal
- [ ] Frontend: Ctrl+C in terminal

---

## 🚀 Next Steps

1. **Complete Phase 1 Testing**
   - Test all user roles
   - Upload various document types
   - Verify all workflows

2. **Run Cleanup**
   ```bash
   cleanup.bat
   ```

3. **Prepare for Phase 2**
   - Review roadmap in [README.md](README.md)
   - Plan workflow automation
   - Set up production environment

4. **Production Deployment** (Phase 2+)
   - Set up cloud database (Azure/AWS)
   - Configure Google Drive integration
   - Set up CI/CD pipeline
   - Add HTTPS and domain

---

**Version**: 1.0.0 | **Status**: Phase 1 Complete ✅ | **Updated**: January 2026
