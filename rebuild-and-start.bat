@echo off
echo ========================================
echo  Rebuilding and Restarting ROMS
echo ========================================
echo.

echo [1/2] Building application...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Starting application...
call .\start-roms.bat
