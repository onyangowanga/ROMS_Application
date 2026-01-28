# ROMS Quick Start Guide

> **Phase 1 Complete** ✅ | Get running in **~5 minutes** with Docker

## 🚀 Getting Started

### Prerequisites
- **Docker Desktop** (for Windows/Mac) or Docker Engine (Linux)
- **Node.js** 18+ (for frontend)
- **Java** 17+ (installed with Maven wrapper included)

Check installations:
```bash
docker --version    # Should be 20.x+
node --version      # Should be v18.x+
java -version       # Should be 17.x+
```

---

## ⚡ 5-Minute Setup

### Step 1: Clone & Navigate
```bash
cd c:\Programing\Realtime projects\ROMS\Roms\Roms
```

### Step 2: Start Database (Docker)
```bash
docker-compose -f docker-compose.dev.yml up -d
```

**Services Started:**
- PostgreSQL 16 on port **5433**
- pgAdmin on port **5050** (admin@roms.com / admin)

**Database auto-created:**
- Database: `roms_db`
- User: `postgres`
- Password: `JonaMia`

### Step 3: Start Backend
```bash
mvnw.cmd spring-boot:run
```

**Backend running:** http://localhost:8080  
**Auto-reload:** Enabled (changes apply automatically)

### Step 4: Install Frontend Dependencies
```bash
cd frontend
npm install
```

### Step 5: Start Frontend
```bash
npm run dev
```

**Frontend running:** http://localhost:3002  
**Hot reload:** Enabled (instant updates)

---

## 🎯 Access the Application

### Main Application
**URL:** http://localhost:3002

### Test Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | password123 | SUPER_ADMIN |
| operations | password123 | OPERATIONS_STAFF |
| finance | password123 | FINANCE_MANAGER |

### pgAdmin (Database Management)
**URL:** http://localhost:5050
- Email: admin@roms.com
- Password: admin

**Add Server in pgAdmin:**
1. Right-click "Servers" → "Register" → "Server"
2. **General Tab:** Name = "ROMS Dev"
3. **Connection Tab:**
   - Host: `host.docker.internal` (or `postgres` if inside Docker)
   - Port: `5432` (internal) or `5433` (from host)
   - Database: `roms_db`
   - Username: `postgres`
   - Password: `JonaMia`

---

## 📝 Quick Test Workflow

### 1. Login as Admin
1. Go to http://localhost:3002
2. Click **"Login"**
3. Username: `admin`, Password: `password123`
4. Click **"Sign In"**

### 2. View Job Orders
1. Click **"Jobs"** in navigation
2. You should see 8 test jobs:
   - Mechanical Engineer (Dubai)
   - Chef (Dubai)
   - Civil Engineer (Abu Dhabi)
   - Nurse (Riyadh)
   - etc.

### 3. Create Applicant
1. Logout (top-right corner)
2. Click **"Apply for Job"**
3. Fill registration form:
   - Email: test@example.com
   - Password: password123
   - First Name: John
   - Last Name: Doe
   - Passport No: AB123456
   - Phone: +971501234567
   - Select a job from dropdown
4. Click **"Register"**

### 4. Upload Documents (as Applicant)
1. Login with applicant credentials
2. Go to **"My Application"**
3. Click **"Upload Document"**
4. Choose file type (Passport, Resume, etc.)
5. Select file (PDF, JPG, PNG, DOCX)
6. Click **"Upload"**
7. Documents saved to `/uploads/` folder

### 5. View Candidates (as Admin)
1. Logout and login as `admin`
2. Click **"Candidates"**
3. See all registered applicants
4. View candidate details
5. See uploaded documents

---

## 🔧 Development Commands

### Backend
```bash
# Start with auto-reload
mvnw.cmd spring-boot:run

# Clean build
mvnw.cmd clean install

# Run tests
mvnw.cmd test
```

### Frontend
```bash
cd frontend

# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Database
```bash
# Start containers
docker-compose -f docker-compose.dev.yml up -d

# Stop containers
docker-compose -f docker-compose.dev.yml down

# View logs
docker logs roms-postgres-dev

# Restart database
docker-compose -f docker-compose.dev.yml restart postgres
```

---

## 📡 API Testing (cURL)

### 1. Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\",\"fullName\":\"Test User\",\"role\":\"APPLICANT\"}"
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"password123\"}"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "SUPER_ADMIN"
    }
  }
}
```

**Copy the `accessToken` for next requests!**

### 3. Create Candidate (with JWT)
```bash
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane@example.com\",\"passportNo\":\"CD789012\",\"phoneNumber\":\"+971501234567\",\"jobOrderId\":1}"
```

### 4. Get All Candidates
```bash
curl -X GET http://localhost:8080/api/candidates \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### 4. Get All Candidates
```bash
curl -X GET http://localhost:8080/api/candidates \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```



### 5. Upload Document
```bash
# Upload passport scan for candidate
curl -X POST "http://localhost:8080/api/candidates/1/documents?docType=PASSPORT" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -F "file=@/path/to/passport.pdf"
```

---

## 🧹 Cleanup & Maintenance

### Remove Redundant Files
```bash
# Run cleanup script to remove Phase 1 development artifacts
cleanup.bat
```

**Removes:**
- Temporary files (logs, compile errors)
- Outdated documentation
- Redundant scripts
- Old SQL files

### Database Reset
```bash
# Stop and remove database volumes
docker-compose -f docker-compose.dev.yml down -v

# Start fresh
docker-compose -f docker-compose.dev.yml up -d

# Restart backend (JPA will recreate schema)
mvnw.cmd spring-boot:run
```

### View Logs
```bash
# Backend logs (check terminal where mvnw is running)

# Database logs
docker logs roms-postgres-dev

# Frontend logs (check terminal where npm run dev is running)
```

---

## 🐛 Troubleshooting

### Port 5433 Already in Use
```bash
# Find process
netstat -ano | findstr :5433

# Stop containers
docker-compose -f docker-compose.dev.yml down
```

### Backend Won't Start
```bash
# Verify database is running
docker ps

# Check credentials in application.yaml:
# - username: postgres
# - password: JonaMia
# - port: 5433
```

### Frontend Build Errors
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Database Connection Refused
```bash
# Restart PostgreSQL
docker-compose -f docker-compose.dev.yml restart postgres

# Wait 10 seconds, then restart backend
```

### Document Upload Fails
- **File size**: Max 10MB
- **File types**: PDF, JPG, PNG, DOCX only
- **Permissions**: Applicants upload to own records only
- **uploads/ folder**: Must exist and be writable

---

## 📚 Next Steps

1. **Explore Features**: See [FRONTEND_FEATURES.md](FRONTEND_FEATURES.md)
2. **Architecture**: See [ARCHITECTURE.md](ARCHITECTURE.md)
3. **Phase 1 Summary**: See [PHASE_1_COMPLETE.md](PHASE_1_COMPLETE.md)
4. **Test Workflows**: Create candidates, upload documents, manage jobs
5. **Phase 2 Planning**: Review roadmap in [README.md](README.md)

---

**Need Help?**
- Detailed docs: [README.md](README.md)
- Check logs in terminal
- Browser console: F12
- Verify services: `docker ps`

---

**Version**: 1.0.0 | **Status**: Phase 1 Complete ✅ | **Updated**: January 2026
