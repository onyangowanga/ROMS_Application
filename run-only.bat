@echo off
REM ====================================================================
REM ROMS Run Script
REM This script runs the pre-built JAR file without rebuilding
REM ====================================================================

echo.
echo ========================================
echo   ROMS - Run Existing JAR
echo ========================================
echo.

REM Set JDK 17 Path
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Check if JAR exists
if not exist "target\Roms-0.0.1-SNAPSHOT.jar" (
    echo ERROR: JAR file not found!
    echo Please run build-only.bat or start-roms.bat first
    pause
    exit /b 1
)

REM Run Application
echo Starting ROMS application...
echo Application will start on http://localhost:8080
echo Press Ctrl+C to stop the application
echo.
echo ========================================
echo.

java -jar target\Roms-0.0.1-SNAPSHOT.jar

pause
