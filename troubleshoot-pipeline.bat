@echo off
REM ========================================
REM Jenkins Pipeline Troubleshooting Script
REM ========================================
echo.
echo Starting comprehensive troubleshooting...
echo.

REM Create troubleshooting log
set LOGFILE=troubleshoot-log.txt
echo Jenkins Pipeline Troubleshooting Log > %LOGFILE%
echo Generated: %DATE% %TIME% >> %LOGFILE%
echo ======================================== >> %LOGFILE%
echo. >> %LOGFILE%

echo [STEP 1/8] Checking Java Installation...
echo. >> %LOGFILE%
echo [JAVA] >> %LOGFILE%
java -version 2>&1 | tee -a %LOGFILE%
if errorlevel 1 (
    echo [ERROR] Java not found!
    echo [ERROR] Java not found! >> %LOGFILE%
    goto :END
) else (
    echo [OK] Java is installed
)
echo.

echo [STEP 2/8] Checking Maven Installation...
echo [MAVEN] >> %LOGFILE%
mvn -v 2>&1 | tee -a %LOGFILE%
if errorlevel 1 (
    echo [ERROR] Maven not found!
    echo [ERROR] Maven not found! >> %LOGFILE%
    goto :END
) else (
    echo [OK] Maven is installed
)
echo.

echo [STEP 3/8] Checking Chrome Installation...
echo [CHROME] >> %LOGFILE%
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe" 2>nul
if errorlevel 1 (
    echo [WARNING] Chrome not found in registry
    echo [WARNING] Chrome not found in registry >> %LOGFILE%
    echo Selenium tests will fail without Chrome
) else (
    echo [OK] Chrome is installed
    echo [OK] Chrome is installed >> %LOGFILE%
)
echo.

echo [STEP 4/8] Checking Docker...
echo [DOCKER] >> %LOGFILE%
docker --version 2>&1 >> %LOGFILE%
if errorlevel 1 (
    echo [WARNING] Docker not available
    echo [WARNING] Docker not available >> %LOGFILE%
) else (
    echo [OK] Docker version found
)

docker ps >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Docker daemon not running
    echo [WARNING] Docker daemon not running >> %LOGFILE%
) else (
    echo [OK] Docker daemon is running
    echo [OK] Docker daemon is running >> %LOGFILE%
)
echo.

echo [STEP 5/8] Checking Port Availability...
echo [PORTS] >> %LOGFILE%
netstat -an | findstr ":8800" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [WARNING] Port 8800 is already in use!
    echo [WARNING] Port 8800 is already in use! >> %LOGFILE%
    netstat -ano | findstr ":8800" | findstr "LISTENING"
) else (
    echo [OK] Port 8800 is available
    echo [OK] Port 8800 is available >> %LOGFILE%
)
echo.

echo [STEP 6/8] Testing Application Locally...
echo [APP TEST] >> %LOGFILE%
echo Building application...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Maven build failed!
    echo [ERROR] Maven build failed! >> %LOGFILE%
    goto :END
) else (
    echo [OK] Build successful
    echo [OK] Build successful >> %LOGFILE%
)

echo Starting application on port 8800...
start /B java -jar target\devops-pipeline-app-1.0.0.jar > test-app.log 2>&1

echo Waiting 15 seconds for application to start...
timeout /t 15 /nobreak >nul

echo Testing health endpoint...
curl -s http://localhost:8800/health
if errorlevel 1 (
    echo [ERROR] Application health check failed!
    echo [ERROR] Application health check failed! >> %LOGFILE%
    echo Application logs:
    type test-app.log
    type test-app.log >> %LOGFILE%
    goto :CLEANUP
) else (
    echo [OK] Application is running and healthy!
    echo [OK] Application is running and healthy! >> %LOGFILE%
)
echo.

echo [STEP 7/8] Testing Selenium Locally...
echo [SELENIUM TEST] >> %LOGFILE%
echo Running Selenium test...
call mvn test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true 2>&1
if errorlevel 1 (
    echo [ERROR] Selenium test failed!
    echo [ERROR] Selenium test failed! >> %LOGFILE%
    echo Check test output above for details
) else (
    echo [OK] Selenium test passed!
    echo [OK] Selenium test passed! >> %LOGFILE%
)
echo.

echo [STEP 8/8] Testing Docker Build...
echo [DOCKER BUILD] >> %LOGFILE%
docker build -t test-devops-app . >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Docker build failed
    echo [WARNING] Docker build failed >> %LOGFILE%
    echo Either Docker not available or Dockerfile has issues
) else (
    echo [OK] Docker build successful
    echo [OK] Docker build successful >> %LOGFILE%
    docker images test-devops-app
)
echo.

:CLEANUP
echo Cleaning up test application...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8800" ^| find "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
echo Application stopped.
echo.

:END
echo ========================================
echo Troubleshooting Complete!
echo.
echo Full log saved to: %LOGFILE%
echo.
echo NEXT STEPS:
echo 1. Review the log file: %LOGFILE%
echo 2. Check Jenkins Console Output for specific errors
echo 3. If Selenium failed: Ensure Chrome is installed
echo 4. If Docker failed: Ensure Docker Desktop is running
echo 5. If app health check failed: Check port conflicts
echo.
echo For detailed Jenkins logs:
echo   - Go to Jenkins ^> Job ^> Latest Build
echo   - Click "Console Output"
echo   - Look for ERROR or FAILED messages
echo.
pause
