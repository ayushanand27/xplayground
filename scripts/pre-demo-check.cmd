@echo off
setlocal

cd /d "%~dp0.."

echo ===== Docker =====
docker info >nul 2>&1
if errorlevel 1 (
    echo FAIL: Docker daemon is down.
    exit /b 1
)
echo OK: Docker daemon is reachable.

echo.
echo ===== Services =====
docker compose ps

echo.
echo ===== Health =====
call :check "Backend" "http://localhost:8800/health"
if errorlevel 1 exit /b 1
call :check "Prometheus" "http://localhost:9090/-/healthy"
if errorlevel 1 exit /b 1
call :check "Grafana" "http://localhost:3001/api/health"
if errorlevel 1 exit /b 1
call :check "Frontend" "http://localhost:3000"
if errorlevel 1 exit /b 1

echo.
echo ===== Prometheus Query =====
curl --max-time 5 -G -s --data-urlencode "query=up{job=\"backend\"}" http://localhost:9090/api/v1/query
echo.

echo [SUCCESS] Pre-demo check passed.
exit /b 0

:check
set "NAME=%~1"
set "URL=%~2"

curl --max-time 5 -fsS "%URL%" >nul 2>&1
if errorlevel 1 (
    echo FAIL: %NAME% ^(%URL%^)
    exit /b 1
)

echo OK: %NAME%
exit /b 0
