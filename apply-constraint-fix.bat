@echo off
echo Fixing candidate status constraint...
echo.

REM Update with your PostgreSQL bin path if needed
set PGPASSWORD=JonaMia
"C:\Program Files\PostgreSQL\18\bin\psql.exe" -h 127.0.0.1 -p 5433 -U postgres -d roms_db -f fix-candidate-status-constraint.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Constraint updated successfully!
) else (
    echo.
    echo Error updating constraint. Please run the SQL manually.
)

pause
