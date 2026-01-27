@echo off
echo ========================================
echo ROMS Frontend - Production Build
echo ========================================
echo.

cd /d "%~dp0"

echo Building production bundle...
call npm run build

if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build Complete!
echo ========================================
echo Output directory: dist/
echo.
echo To preview production build, run:
echo   npm run preview
echo.
pause
