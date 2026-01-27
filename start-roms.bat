@echo off
REM ====================================================================
REM ROMS Application Starter
REM This script builds and runs the ROMS application with JDK 17
REM ====================================================================

echo.
echo ========================================
echo   ROMS - Starting Application
echo ========================================
echo.

REM Set JDK 17 Path
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Verify Java version
echo [1/3] Verifying Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found! Please install JDK 17
    pause
    exit /b 1
)
echo.

REM Clean and Build
echo [2/3] Building application...
echo Running: mvn clean package -DskipTests
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo Build completed successfully!
echo.

REM Run Application
echo [3/3] Starting ROMS application...
echo Application will start on http://localhost:8080
echo Press Ctrl+C to stop the application
echo.
echo ========================================
echo.

java -jar target\Roms-0.0.1-SNAPSHOT.jar

pause
