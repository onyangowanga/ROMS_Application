@echo off
ECHO ================================================
ECHO ROMS - Recruitment Operations Management System
ECHO Setup Script for Windows
ECHO ================================================
ECHO.

REM Check Java version
ECHO [1/6] Checking Java installation...
java -version 2>&1 | findstr /i "version" > nul
IF %ERRORLEVEL% NEQ 0 (
    ECHO [ERROR] Java is not installed or not in PATH
    ECHO Please install Java 17 or higher from: https://adoptium.net/
    pause
    exit /b 1
)
ECHO [OK] Java is installed
ECHO.

REM Check Maven
ECHO [2/6] Checking Maven installation...
mvn -v 2>&1 | findstr /i "Maven" > nul
IF %ERRORLEVEL% NEQ 0 (
    ECHO [ERROR] Maven is not installed or not in PATH
    ECHO Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
ECHO [OK] Maven is installed
ECHO.

REM Check PostgreSQL
ECHO [3/6] Checking PostgreSQL installation...
psql --version 2>&1 | findstr /i "psql" > nul
IF %ERRORLEVEL% NEQ 0 (
    ECHO [WARNING] PostgreSQL is not installed or not in PATH
    ECHO Please install PostgreSQL 15+ from: https://www.postgresql.org/download/windows/
    ECHO.
    ECHO Alternative: Use Docker Compose to run PostgreSQL
    ECHO   docker-compose up -d
    ECHO.
) ELSE (
    ECHO [OK] PostgreSQL is installed
)
ECHO.

REM Build project
ECHO [4/6] Building project with Maven...
call mvn clean install -DskipTests
IF %ERRORLEVEL% NEQ 0 (
    ECHO [ERROR] Build failed
    pause
    exit /b 1
)
ECHO [OK] Build successful
ECHO.

REM Setup environment file
ECHO [5/6] Setting up environment configuration...
IF NOT EXIST ".env" (
    IF EXIST ".env.example" (
        copy .env.example .env
        ECHO [OK] Created .env file from template
        ECHO [ACTION REQUIRED] Please edit .env file with your configuration
    ) ELSE (
        ECHO [WARNING] .env.example not found
    )
) ELSE (
    ECHO [OK] .env file already exists
)
ECHO.

REM Instructions
ECHO [6/6] Setup complete!
ECHO.
ECHO ================================================
ECHO Next Steps:
ECHO ================================================
ECHO.
ECHO 1. Make sure PostgreSQL is running
ECHO    - Windows: Check Services (services.msc)
ECHO    - Or use Docker: docker-compose up -d
ECHO.
ECHO 2. Update configuration in:
ECHO    - src/main/resources/application.yaml
ECHO    - .env (if using environment variables)
ECHO.
ECHO 3. Create database (if not using Docker):
ECHO    psql -U postgres
ECHO    CREATE DATABASE roms_db;
ECHO    CREATE USER roms_user WITH PASSWORD 'roms_password';
ECHO    GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;
ECHO.
ECHO 4. Run the application:
ECHO    mvn spring-boot:run
ECHO.
ECHO 5. Access the API:
ECHO    http://localhost:8080/api
ECHO.
ECHO 6. Quick test:
ECHO    See QUICKSTART.md for API examples
ECHO.
ECHO ================================================
pause
