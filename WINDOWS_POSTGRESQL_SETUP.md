# PostgreSQL Windows Installation Guide for ROMS

This guide will help you install and configure PostgreSQL directly on Windows.

## Step 1: Download PostgreSQL

1. Visit: https://www.postgresql.org/download/windows/
2. Click "Download the installer"
3. Download **PostgreSQL 15.x** for Windows x86-64
4. Recommended: Use the EnterpriseDB (EDB) installer

Direct download: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads

## Step 2: Install PostgreSQL

1. **Run the installer** as Administrator
2. **Installation Directory**: Default (`C:\Program Files\PostgreSQL\15`)
3. **Components to Install**:
   - ✅ PostgreSQL Server
   - ✅ pgAdmin 4
   - ✅ Stack Builder (optional)
   - ✅ Command Line Tools

4. **Data Directory**: Default (`C:\Program Files\PostgreSQL\15\data`)

5. **Password**: 
   - Set superuser (postgres) password: `admin` (or your choice)
   - **REMEMBER THIS PASSWORD** - you'll need it!

6. **Port**: `5432` (default - must match application.yaml)

7. **Locale**: Default (`[Default locale]`)

8. **Complete the installation** and **uncheck** "Launch Stack Builder" at the end

## Step 3: Verify Installation

Open Command Prompt and verify:

```cmd
"C:\Program Files\PostgreSQL\15\bin\psql" --version
```

Expected output:
```
psql (PostgreSQL) 15.x
```

## Step 4: Create ROMS Database

### Option A: Using Command Line

1. Open Command Prompt as Administrator

2. Navigate to PostgreSQL bin directory:
```cmd
cd "C:\Program Files\PostgreSQL\15\bin"
```

3. Run the setup script:
```cmd
psql -U postgres -f "C:\Programing\Realtime projects\ROMS\Roms\Roms\setup-windows-postgresql.sql"
```

4. Enter the postgres password when prompted (the one you set during installation)

### Option B: Using pgAdmin 4

1. **Open pgAdmin 4** (from Start Menu)

2. **Connect to PostgreSQL**:
   - Expand "Servers" in the left panel
   - Right-click "PostgreSQL 15"
   - Enter your postgres password
   - Click "OK"

3. **Open Query Tool**:
   - Right-click "PostgreSQL 15"
   - Select "Query Tool"

4. **Run Setup Script**:
   - Click "Open File" icon
   - Navigate to: `C:\Programing\Realtime projects\ROMS\Roms\Roms\setup-windows-postgresql.sql`
   - Click "Execute" (Play button or F5)

5. **Verify**:
   - You should see "Query returned successfully"
   - Check the "Databases" section - you should see `roms_db`

## Step 5: Test Connection

Test the connection from command line:

```cmd
cd "C:\Program Files\PostgreSQL\15\bin"
set PGPASSWORD=roms_password
psql -h localhost -U roms_user -d roms_db -c "SELECT version();"
```

Expected output:
```
                                  version                                   
----------------------------------------------------------------------------
 PostgreSQL 15.x on x86_64-pc-windows-msvc, compiled by Visual C++ build...
(1 row)
```

## Step 6: Update Application Configuration

The `application.yaml` is already configured for localhost:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/roms_db
    username: roms_user
    password: roms_password
```

## Step 7: Run ROMS Application

1. Rebuild the application:
```cmd
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
mvn clean package -DskipTests
```

2. Run the application:
```cmd
"C:\Program Files\Java\jdk-17\bin\java" -jar target\Roms-0.0.1-SNAPSHOT.jar
```

3. **Look for successful startup**:
```
Started RomsApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

## Troubleshooting

### Port 5432 Already in Use

If you get "port 5432 is already in use":

```cmd
netstat -ano | findstr :5432
```

Find the process ID (PID) and stop it, or change PostgreSQL port.

### Connection Refused

1. Check if PostgreSQL is running:
```cmd
sc query postgresql-x64-15
```

2. Start PostgreSQL service:
```cmd
net start postgresql-x64-15
```

### Authentication Failed

1. Verify password in pgAdmin
2. Try resetting the password:
```sql
ALTER USER roms_user WITH PASSWORD 'roms_password';
```

### Service Not Starting

1. Open Services (Win + R, type `services.msc`)
2. Find "postgresql-x64-15"
3. Right-click → Properties
4. Startup type: Automatic
5. Click "Start"

## Database Credentials Summary

- **Database Name**: `roms_db`
- **Username**: `roms_user`
- **Password**: `roms_password`
- **Host**: `localhost` or `127.0.0.1`
- **Port**: `5432`

## Next Steps

After successful connection:
1. Application will automatically create tables (using JPA DDL auto-update)
2. Follow [LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md) to test the application
3. Register your first admin user
4. Start testing the APIs

---

## Returning to Docker Later

When you're ready to switch back to Docker:

1. Stop Windows PostgreSQL service:
```cmd
net stop postgresql-x64-15
```

2. Start Docker containers:
```cmd
docker-compose up -d
```

3. The application will automatically connect to Docker PostgreSQL

---

Good luck! 🚀
