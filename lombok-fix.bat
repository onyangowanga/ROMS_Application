@echo off
REM LOMBOK FIX FOR MAVEN BUILD
REM This script attempts to fix Lombok compilation issues

echo ====================================
echo ROMS Lombok Build Fix Script
echo ====================================
echo.

cd /d "C:\Programing\Realtime projects\ROMS\Roms\Roms"

echo Step 1: Cleaning Maven cache for this project...
rd /s /q target 2>nul
del /f /q .classpath 2>nul
del /f /q .project 2>nul
rd /s /q .settings 2>nul

echo Step 2: Downloading fresh dependencies...
call mvnw.cmd dependency:purge-local-repository -DactTransitively=false -DreResolve=false

echo Step 3: Attempting clean compile...
call mvnw.cmd clean compile

echo.
echo ====================================
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS! Build completed.
    echo You can now run: mvnw.cmd spring-boot:run
) else (
    echo BUILD FAILED - See errors above
    echo.
    echo The project has Lombok annotation processing issues.
    echo Recommendation: Try building in VS Code with its Maven extension,
    echo or use IntelliJ IDEA which has better Lombok support.
)
echo ====================================
pause
