@echo off
echo ========================================
echo  Adding Assignment Uniqueness Index
echo ========================================
echo.

REM Set PostgreSQL password
set PGPASSWORD=JonaMia

echo Running index creation script...
psql -U postgres -d roms_db -p 5433 -f add-assignment-uniqueness-index.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  Index created successfully!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo  Failed to create index
    echo ========================================
)

pause
