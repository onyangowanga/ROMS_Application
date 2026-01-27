@echo off
REM ====================================================================
REM ROMS Build Script
REM This script only builds the application without running it
REM ====================================================================

echo.
echo ========================================
echo   ROMS - Build Only
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

REM Clean and Build
echo [2/2] Building application...
echo Running: mvn clean package -DskipTests
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Build Successful!
echo   JAR: target\Roms-0.0.1-SNAPSHOT.jar
echo ========================================
echo.

pause
