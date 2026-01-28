@echo off
REM ====================================================================
REM ROMS - Cleanup Redundant Files (Phase 1 Completion)
REM This script removes outdated documentation and temporary files
REM ====================================================================

echo.
echo ========================================
echo   ROMS - Project Cleanup
echo ========================================
echo.

echo [1/4] Removing temporary files...
del /q temp_pgpass.txt 2>nul
del /q temp_token.txt 2>nul
del /q compile-errors.txt 2>nul
del /q app.log 2>nul

echo [2/4] Removing outdated documentation...
del /q BUILD_FIX_README.md 2>nul
del /q JAVA_VERSION_FIX.md 2>nul
del /q CORRECTIONS_APPLIED.md 2>nul
del /q REFACTORING_SUMMARY.md 2>nul
del /q PHASE_1_5_DELIVERY_SUMMARY.md 2>nul
del /q PHASE_1_5_IMPLEMENTATION.md 2>nul
del /q PHASE_1_5_TESTING_GUIDE.md 2>nul
del /q PHASE_1B_TESTING_GUIDE.md 2>nul
del /q WINDOWS_POSTGRESQL_SETUP.md 2>nul
del /q MIGRATION_GUIDE.md 2>nul

echo [3/4] Removing redundant batch scripts...
del /q build-only.bat 2>nul
del /q clean.bat 2>nul
del /q run-only.bat 2>nul
del /q run-test-data.bat 2>nul
del /q setup.bat 2>nul
del /q USE_JDK17.bat 2>nul
del /q start-dev.bat 2>nul
del /q test-db-connection.bat 2>nul

echo [4/4] Removing redundant SQL scripts...
del /q check-candidates.sql 2>nul
del /q populate-test-data.sql 2>nul
del /q reset-demo-users.sql 2>nul
del /q setup-database.sql 2>nul
del /q setup-windows-postgresql.sql 2>nul

echo.
echo ========================================
echo   Cleanup Complete!
echo ========================================
echo.
echo Remaining essential files:
echo   - start-roms.bat (start backend)
echo   - docker-compose.dev.yml (PostgreSQL)
echo   - insert-jobs.sql (test data)
echo   - database-schema.sql (reference)
echo.
echo Updated documentation:
echo   - PHASE_1_COMPLETE.md (summary)
echo   - README.md (main docs)
echo   - QUICKSTART.md (setup guide)
echo.

pause
