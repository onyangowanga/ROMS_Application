@echo off
REM Set JAVA_HOME to JDK 17 for this session only
REM Update the path below after installing JDK 17

SET JAVA_HOME=C:\Program Files\Java\jdk-17
SET PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo ========================================
echo Java Environment Configured for ROMS
echo ========================================
java -version
echo.
echo JAVA_HOME=%JAVA_HOME%
echo.
echo You can now run:
echo   mvn clean install
echo   mvn spring-boot:run
echo ========================================
echo.
