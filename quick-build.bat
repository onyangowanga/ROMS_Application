@echo off
echo Building ROMS application...
call mvnw.cmd clean package -DskipTests
echo.
echo Build complete. Check for errors above.
pause
