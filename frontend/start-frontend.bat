@echo off
echo ========================================
echo ROMS Frontend - Development Server
echo ========================================
echo.

cd /d "%~dp0"

echo Checking Node.js installation...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed!
    echo Please install Node.js 18+ from https://nodejs.org
    pause
    exit /b 1
)

echo Node.js found: 
node --version
echo.

if not exist "node_modules\" (
    echo Installing dependencies...
    call npm install
    if errorlevel 1 (
        echo ERROR: Failed to install dependencies
        pause
        exit /b 1
    )
    echo.
)

echo Starting development server...
echo Frontend will be available at: http://localhost:3000
echo Backend proxy: http://localhost:8080
echo.
echo Press Ctrl+C to stop the server
echo.

call npm run dev

pause
