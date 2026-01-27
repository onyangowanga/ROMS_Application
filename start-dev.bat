@echo off
REM ====================================================================
REM ROMS Development Mode Starter
REM This script runs using Spring Boot Maven plugin with hot reload
REM Static files (HTML/CSS/JS) changes will be picked up automatically
REM ====================================================================

echo.
echo ========================================
echo   ROMS - Development Mode
echo ========================================
echo.

REM Set JDK 17 Path
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Verify Java version
echo [1/2] Verifying Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found! Please install JDK 17
    pause
    exit /b 1
)
echo.

REM Run with Spring Boot Maven plugin (supports hot reload)
echo [2/2] Starting in Development Mode...
echo Hot reload enabled - static files will update without restart
echo Application will start on http://localhost:8080
echo Press Ctrl+C to stop
echo.
echo ========================================
echo.

call mvn spring-boot:run

pause
