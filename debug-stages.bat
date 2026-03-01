@echo off
REM ========================================
REM Quick Jenkins Stage Debugger
REM Tests each stage independently
REM ========================================

echo.
echo ╔════════════════════════════════════════╗
echo ║  Jenkins Pipeline Stage Debugger       ║
echo ╚════════════════════════════════════════╝
echo.

:MENU
echo.
echo Select which stage to debug:
echo.
echo  [1] Test Selenium Stage (most likely issue)
echo  [2] Test Docker Stage
echo  [3] Test Complete Pipeline Locally
echo  [4] View Jenkins Console Output Guide
echo  [5] Check System Requirements
echo  [6] Exit
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" goto SELENIUM
if "%choice%"=="2" goto DOCKER
if "%choice%"=="3" goto FULLTEST
if "%choice%"=="4" goto JENKINS_GUIDE
if "%choice%"=="5" goto SYSCHECK
if "%choice%"=="6" goto EOF
echo Invalid choice. Please try again.
goto MENU

:SELENIUM
echo.
echo ════════════════════════════════════════
echo Testing Selenium Stage
echo ════════════════════════════════════════
echo.

echo Step 1: Check if Chrome is installed...
where chrome.exe >nul 2>&1
if errorlevel 1 (
    reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe" >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Chrome not found!
        echo.
        echo SOLUTION:
        echo 1. Download Chrome: https://www.google.com/chrome/
        echo 2. Install Chrome to default location
        echo 3. Restart this script
        echo.
        pause
        goto MENU
    )
)
echo [OK] Chrome found

echo.
echo Step 2: Starting application...
start /B java -jar target\devops-pipeline-app-1.0.0.jar > selenium-test.log 2>&1
echo Waiting 15 seconds for app to initialize...
timeout /t 15 /nobreak >nul

echo.
echo Step 3: Testing application health...
curl -s http://localhost:8800/health
if errorlevel 1 (
    echo [ERROR] App not responding!
    echo.
    echo Application logs:
    type selenium-test.log
    echo.
    goto CLEANUP_SELENIUM
)
echo [OK] App is healthy

echo.
echo Step 4: Running Selenium test...
echo (This will open a headless Chrome browser)
echo.
mvn test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true

echo.
echo ════════════════════════════════════════
if errorlevel 1 (
    echo RESULT: Selenium test FAILED ❌
    echo.
    echo COMMON ISSUES:
    echo 1. Chrome not installed or not in PATH
    echo 2. ChromeDriver compatibility issue
    echo 3. Application not fully started
    echo 4. Firewall blocking localhost
    echo.
    echo CHECK JENKINS:
    echo - Go to Jenkins console output
    echo - Look for "org.openqa.selenium" errors
    echo - Check if Chrome version matches ChromeDriver
) else (
    echo RESULT: Selenium test PASSED ✓
    echo.
    echo This means Selenium works locally but fails in Jenkins.
    echo.
    echo JENKINS-SPECIFIC ISSUES:
    echo 1. Jenkins may not have Chrome in its PATH
    echo 2. Jenkins user may lack permissions
    echo 3. Headless mode may not work in Jenkins environment
    echo.
    echo SOLUTION:
    echo Add to Jenkinsfile before Selenium stage:
    echo   bat 'where chrome.exe'
    echo   bat 'echo %%PATH%%'
)
echo ════════════════════════════════════════
echo.

:CLEANUP_SELENIUM
echo Stopping application...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8800" ^| find "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
echo Done.
pause
goto MENU

:DOCKER
echo.
echo ════════════════════════════════════════
echo Testing Docker Stage
echo ════════════════════════════════════════
echo.

echo Step 1: Check Docker installation...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker not installed!
    echo.
    echo SOLUTION:
    echo 1. Download Docker Desktop: https://www.docker.com/products/docker-desktop
    echo 2. Install Docker Desktop
    echo 3. Start Docker Desktop
    echo 4. Run this script again
    echo.
    pause
    goto MENU
)
echo [OK] Docker installed

echo.
echo Step 2: Check Docker daemon...
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker daemon not running!
    echo.
    echo SOLUTION:
    echo 1. Open Docker Desktop application
    echo 2. Wait for Docker icon to stop animating (fully started)
    echo 3. Run this script again
    echo.
    echo If Docker Desktop is running:
    echo - Try: wsl --shutdown (then restart Docker Desktop)
    echo - Try: Restart Docker Desktop
    echo - Try: Restart computer
    echo.
    pause
    goto MENU
)
echo [OK] Docker daemon running

echo.
echo Step 3: Building Docker image...
docker build -t devops-pipeline-app:test .
if errorlevel 1 (
    echo [ERROR] Docker build failed!
    echo.
    echo Check errors above. Common issues:
    echo 1. Dockerfile syntax error
    echo 2. Missing files referenced in Dockerfile
    echo 3. Network issues downloading base image
    echo.
    pause
    goto MENU
)

echo.
echo [SUCCESS] Docker build completed!
echo.
echo Image details:
docker images devops-pipeline-app:test
echo.

echo Test running the container? (y/n)
set /p runtest="Run test container? (y/n): "
if /i "%runtest%"=="y" (
    echo Running container...
    docker run --rm -p 8900:8800 devops-pipeline-app:test
)

echo.
pause
goto MENU

:FULLTEST
echo.
echo ════════════════════════════════════════
echo Running Complete Pipeline Test
echo ════════════════════════════════════════
echo.
echo This simulates the Jenkins pipeline locally.
echo.

echo Stage 1: Checkout (skipped - already have code)
echo [OK]

echo.
echo Stage 2: Build
mvn clean compile -q
if errorlevel 1 (
    echo [FAILED] Build stage failed
    pause
    goto MENU
)
echo [OK]

echo.
echo Stage 3: Unit Tests
mvn test -q
if errorlevel 1 (
    echo [FAILED] Unit tests failed
    pause
    goto MENU
)
echo [OK]

echo.
echo Stage 4: Package
mvn package -DskipTests -q
if errorlevel 1 (
    echo [FAILED] Package stage failed
    pause
    goto MENU
)
echo [OK]

echo.
echo Stage 5: Start Application
start /B java -jar target\devops-pipeline-app-1.0.0.jar > full-test.log 2>&1
timeout /t 15 /nobreak >nul
curl -s http://localhost:8800/health >nul
if errorlevel 1 (
    echo [FAILED] App failed to start
    type full-test.log
    pause
    goto CLEANUP_FULL
)
echo [OK]

echo.
echo Stage 6: Selenium UI Test
mvn test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true -q
if errorlevel 1 (
    echo [FAILED] Selenium test failed
) else (
    echo [OK]
)

echo.
echo Stage 7: Docker Build
docker build -t devops-pipeline-app:test . >nul 2>&1
if errorlevel 1 (
    echo [FAILED] Docker build failed
) else (
    echo [OK]
)

:CLEANUP_FULL
echo.
echo Cleaning up...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8800" ^| find "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
echo.
echo Pipeline test complete!
pause
goto MENU

:JENKINS_GUIDE
echo.
echo ════════════════════════════════════════
echo How to Check Jenkins Console Output
echo ════════════════════════════════════════
echo.
echo 1. Open Jenkins: http://localhost:8080
echo.
echo 2. Click on your job name
echo.
echo 3. Click on the latest build number (left sidebar)
echo.
echo 4. Click "Console Output" (left sidebar)
echo.
echo 5. Look for these patterns:
echo.
echo    FOR SELENIUM FAILURES:
echo    - Search for "selenium"
echo    - Look for "org.openqa.selenium.SessionNotCreatedException"
echo    - Look for "ChromeDriver"
echo    - Look for "WebDriverException"
echo.
echo    FOR DOCKER FAILURES:
echo    - Search for "docker"
echo    - Look for "Cannot connect to the Docker daemon"
echo    - Look for "docker: command not found"
echo    - Look for "denied"
echo.
echo 6. Copy the error message and use it to search for solutions
echo.
echo EXAMPLE ERROR PATTERNS:
echo.
echo Selenium Error Example:
echo   org.openqa.selenium.SessionNotCreatedException:
echo   Could not start a new session. Chrome version must be...
echo   ^^ This means ChromeDriver version doesn't match Chrome
echo.
echo Docker Error Example:
echo   Cannot connect to the Docker daemon at...
echo   ^^ This means Docker Desktop is not running
echo.
pause
goto MENU

:SYSCHECK
echo.
echo ════════════════════════════════════════
echo System Requirements Check
echo ════════════════════════════════════════
echo.

echo Checking Java...
java -version 2>&1 | findstr "version"
if errorlevel 1 (
    echo [X] Java NOT found - REQUIRED
) else (
    echo [✓] Java found
)

echo.
echo Checking Maven...
mvn -v 2>&1 | findstr "Maven"
if errorlevel 1 (
    echo [X] Maven NOT found - REQUIRED
) else (
    echo [✓] Maven found
)

echo.
echo Checking Chrome...
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe" >nul 2>&1
if errorlevel 1 (
    echo [X] Chrome NOT found - REQUIRED for Selenium
) else (
    echo [✓] Chrome found
)

echo.
echo Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [X] Docker NOT found - REQUIRED for Docker stage
) else (
    echo [✓] Docker found
)

docker ps >nul 2>&1
if errorlevel 1 (
    echo [X] Docker daemon NOT running
) else (
    echo [✓] Docker daemon running
)

echo.
echo Checking Ports...
netstat -an | findstr ":8800" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [X] Port 8800 already in use
) else (
    echo [✓] Port 8800 available
)

netstat -an | findstr ":8080" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [✓] Port 8080 in use (probably Jenkins - good!)
) else (
    echo [!] Port 8080 not in use (Jenkins not running?)
)

echo.
echo Checking project files...
if exist "Jenkinsfile" (
    echo [✓] Jenkinsfile found
) else (
    echo [X] Jenkinsfile NOT found
)

if exist "pom.xml" (
    echo [✓] pom.xml found
) else (
    echo [X] pom.xml NOT found
)

if exist "target\devops-pipeline-app-1.0.0.jar" (
    echo [✓] JAR file found
) else (
    echo [!] JAR file not found - run: mvn clean package
)

echo.
echo ════════════════════════════════════════
pause
goto MENU

:EOF
echo.
echo Exiting...
exit /b 0
