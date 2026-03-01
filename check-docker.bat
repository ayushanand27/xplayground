@echo off
REM Script to check if Docker is properly installed and running

echo =========================================
echo Docker Environment Check
echo =========================================
echo.

echo [1/4] Checking if Docker is installed...
docker --version 2>nul
if errorlevel 1 (
    echo [ERROR] Docker is not installed or not in PATH
    echo.
    echo Please install Docker Desktop from:
    echo https://www.docker.com/products/docker-desktop
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Docker is installed
)
echo.

echo [2/4] Checking if Docker daemon is running...
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker daemon is not running
    echo.
    echo Please start Docker Desktop and wait for it to fully start
    echo Look for the Docker icon in system tray - it should not be animated
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Docker daemon is running
)
echo.

echo [3/4] Checking Docker info...
docker info | findstr "Server Version"
echo.

echo [4/4] Testing Docker with hello-world...
docker run --rm hello-world >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Could not run test container
    echo This might cause issues with builds
) else (
    echo [OK] Docker can run containers successfully
)
echo.

echo =========================================
echo Docker is ready for Jenkins pipeline!
echo =========================================
echo.

pause
