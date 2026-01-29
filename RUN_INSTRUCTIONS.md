# ROMS Application - Running Instructions

## ✅ Application Status: SUCCESSFULLY RUNNING

This document describes how the ROMS (Recruitment Operations Management System) application was successfully started and is currently running.

---

## 📊 Current Running Services

### 1. PostgreSQL Database
- **Container Name**: `roms-postgres-dev`
- **Status**: ✅ Running (Healthy)
- **Port**: `5433` (host) → `5432` (container)
- **Database**: `roms_db`
- **Credentials**:
  - Username: `postgres`
  - Password: `JonaMia`

### 2. pgAdmin (Database Management)
- **Container Name**: `roms-pgadmin-dev`
- **Status**: ✅ Running
- **URL**: http://localhost:5050
- **Credentials**:
  - Email: `admin@roms.com`
  - Password: `admin`

### 3. Spring Boot Backend
- **Status**: ✅ Running
- **URL**: http://localhost:8080
- **Framework**: Spring Boot 3.2.2
- **Java Version**: OpenJDK 17.0.17 (Temurin)
- **Features**:
  - Auto-reload enabled (Spring DevTools)
  - JPA with Hibernate (schema auto-update)
  - JWT authentication
  - PostgreSQL integration
  - Google Drive API support

### 4. Vite Frontend
- **Status**: ✅ Running
- **URL**: http://localhost:3000
- **Framework**: Vite + React 18 + TypeScript
- **Features**:
  - Hot Module Reload (HMR) enabled
  - API proxy configured (`/api` → `http://localhost:8080`)
  - Tailwind CSS styling

---

## 🚀 How to Start the Application

Follow these steps to start the ROMS application from scratch:

### Step 1: Start the Database

```bash
# Navigate to project root
cd /home/runner/work/ROMS_Application/ROMS_Application

# Start PostgreSQL and pgAdmin using Docker Compose
docker compose -f docker-compose.dev.yml up -d

# Wait for database to be healthy (takes ~10 seconds)
docker ps --filter "name=roms-postgres-dev"
```

**Expected Output**: Container status should show "Up X seconds (healthy)"

### Step 2: Build and Start the Backend

```bash
# Make Maven wrapper executable (if needed)
chmod +x mvnw

# Build the application (skip tests for faster startup)
./mvnw clean package -DskipTests

# Start the Spring Boot application
./mvnw spring-boot:run
```

**Expected Output**: After 20-30 seconds, you should see:
```
Started RomsApplication in X.XXX seconds
```

**Note**: The backend runs as a foreground process. Keep this terminal open.

### Step 3: Install and Start the Frontend

Open a new terminal:

```bash
# Navigate to frontend directory
cd /home/runner/work/ROMS_Application/ROMS_Application/frontend

# Install dependencies (first time only)
npm install

# Start the Vite development server
npm run dev
```

**Expected Output**:
```
VITE v5.x.x  ready in XXX ms

➜  Local:   http://localhost:3000/
```

**Note**: The frontend runs as a foreground process. Keep this terminal open.

---

## 🔍 Verify Services are Running

### Check All Services

```bash
# Check database
docker ps --filter "name=roms-postgres-dev"

# Check backend
curl http://localhost:8080

# Check frontend
curl http://localhost:3000

# Check pgAdmin
curl http://localhost:5050
```

### View Process Status

```bash
# Check backend process
ps aux | grep RomsApplication

# Check frontend process
ps aux | grep vite
```

---

## 🌐 Access the Application

### Main Application URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | Main user interface |
| **Backend API** | http://localhost:8080 | REST API endpoints |
| **pgAdmin** | http://localhost:5050 | Database management |

### Test User Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `password123` | SUPER_ADMIN |
| `operations` | `password123` | OPERATIONS_STAFF |
| `finance` | `password123` | FINANCE_MANAGER |

---

## 🛠️ Common Operations

### Stop the Application

```bash
# Stop backend (Ctrl+C in the backend terminal)

# Stop frontend (Ctrl+C in the frontend terminal)

# Stop database
docker compose -f docker-compose.dev.yml down
```

### Restart a Service

#### Restart Database Only
```bash
docker compose -f docker-compose.dev.yml restart postgres
```

#### Restart Backend
```bash
# Stop the current process (Ctrl+C)
# Then run:
./mvnw spring-boot:run
```

#### Restart Frontend
```bash
# Stop the current process (Ctrl+C)
# Then run:
cd frontend && npm run dev
```

### View Logs

#### Database Logs
```bash
docker logs roms-postgres-dev
docker logs roms-pgadmin-dev
```

#### Backend Logs
The logs are visible in the terminal where you ran `./mvnw spring-boot:run`

#### Frontend Logs
The logs are visible in the terminal where you ran `npm run dev`

---

## 🧪 Testing the Application

### 1. Test Backend API

```bash
# Health check (if actuator is enabled)
curl http://localhost:8080/actuator/health

# Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

### 2. Test Frontend

1. Open browser to http://localhost:3000
2. You should see the ROMS login page
3. Login with test credentials (admin/password123)

### 3. Test Database Connection

1. Open pgAdmin at http://localhost:5050
2. Login with admin@roms.com / admin
3. Register a new server:
   - Name: ROMS Dev
   - Host: `host.docker.internal` (or `localhost` from host)
   - Port: `5433` (from host) or `5432` (from container)
   - Database: `roms_db`
   - Username: `postgres`
   - Password: `JonaMia`

---

## 🐛 Troubleshooting

### Backend Won't Start

**Problem**: Backend fails to connect to database

**Solution**:
```bash
# Verify database is running and healthy
docker ps --filter "name=roms-postgres-dev"

# Check database logs
docker logs roms-postgres-dev

# Ensure database is on port 5433
netstat -tuln | grep 5433
```

### Frontend Shows "Network Error"

**Problem**: Frontend cannot connect to backend

**Solution**:
```bash
# Verify backend is running
curl http://localhost:8080

# Check that Vite proxy is configured (should be in vite.config.ts)
cat frontend/vite.config.ts | grep -A 5 proxy
```

### Port Already in Use

**Problem**: Error: "Address already in use"

**Solution**:
```bash
# Find process using the port
lsof -i :8080   # For backend
lsof -i :3000   # For frontend
lsof -i :5433   # For database

# Stop the conflicting process or choose a different port
```

---

## 📋 Environment Requirements

### Installed Software

- **Java**: OpenJDK 17.0.17 (or compatible JDK 17+)
- **Node.js**: v20.20.0 (or compatible v18+)
- **npm**: 10.8.2
- **Docker**: 28.0.4
- **Docker Compose**: v2.38.2
- **Maven**: 3.9.12 (via wrapper - included in project)

### Check Versions

```bash
java -version
node --version
npm --version
docker --version
docker compose version
```

---

## 📚 Additional Documentation

- **Quick Start Guide**: [QUICKSTART.md](QUICKSTART.md)
- **Architecture Overview**: [ARCHITECTURE.md](ARCHITECTURE.md)
- **Testing Guide**: [LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md)
- **Frontend Features**: [FRONTEND_FEATURES.md](FRONTEND_FEATURES.md)
- **Main README**: [README.md](README.md)
- **Eclipse Setup**: [ECLIPSE_SETUP.md](ECLIPSE_SETUP.md) (for Windows/Eclipse users)

---

## 🎯 Next Steps

1. **Explore the Application**
   - Login to http://localhost:3000
   - Create job orders
   - Register applicants
   - Upload documents

2. **Development Workflow**
   - Backend changes: Auto-reload with Spring DevTools
   - Frontend changes: Hot Module Reload (instant)
   - Database changes: Schema auto-updated by Hibernate

3. **API Development**
   - API endpoints: `/api/*`
   - Authentication: JWT tokens required
   - Documentation: See controller classes in `src/main/java/com/roms/controller/`

4. **Testing**
   - Backend tests: `./mvnw test`
   - Frontend tests: `cd frontend && npm test`

---

## ✅ Success Checklist

- [x] PostgreSQL database running on port 5433
- [x] pgAdmin accessible on port 5050
- [x] Spring Boot backend running on port 8080
- [x] Vite frontend running on port 3000
- [x] All services responding to HTTP requests
- [x] Test accounts available for login

---

**Last Updated**: 2026-01-29  
**Application Version**: 1.5.0  
**Status**: All systems operational ✅
