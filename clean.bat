@echo off
REM ====================================================================
REM ROMS Clean Script
REM This script removes all build artifacts
REM ====================================================================

echo.
echo ========================================
echo   ROMS - Clean Build Artifacts
echo ========================================
echo.

REM Set JDK 17 Path
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Cleaning build artifacts...
call mvn clean

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Clean Successful!
    echo ========================================
) else (
    echo.
    echo ERROR: Clean failed!
)

echo.
pause
