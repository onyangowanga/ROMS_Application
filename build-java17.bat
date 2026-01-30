@echo off
echo ====================================
echo ROMS Build Script
echo ====================================
echo.
echo IMPORTANT: This project has Lombok annotation processing issues with Maven.
echo.
echo RECOMMENDED SOLUTION: Use Eclipse IDE
echo   1. Download Lombok from: https://projectlombok.org/download
echo   2. Run: java -jar lombok.jar
echo   3. Install Lombok into Eclipse
echo   4. Import project in Eclipse (File - Import - Maven - Existing Maven Projects)
echo   5. Run RomsApplication.java
echo.
echo Attempting Maven build anyway...
echo.

REM Set Java 17
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Java Version:
java -version
echo.

REM Build
echo Building with Maven (may fail due to Lombok issue)...
call mvnw.cmd clean package -DskipTests

REM Check result
if exist "target\Roms-0.0.1-SNAPSHOT.jar" (
    echo.
    echo ====================================
    echo BUILD SUCCESS!
    echo ====================================
    echo JAR created: target\Roms-0.0.1-SNAPSHOT.jar
    echo.
    echo To run: java -jar target\Roms-0.0.1-SNAPSHOT.jar
) else (
    echo.
    echo ====================================
    echo BUILD FAILED (Expected - Lombok Issue)
    echo ====================================
    echo.
    echo Please use Eclipse IDE instead:
    echo   See ECLIPSE_SETUP.md for complete instructions
    echo.
)

pause
