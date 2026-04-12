@echo off
setlocal

cd /d "%~dp0.."
echo [INFO] Project root: %CD%

call :waitDocker
if errorlevel 1 (
    echo [WARN] Initial Docker check failed. Attempting recovery...
    call :recoverDocker
    if errorlevel 1 (
        echo [ERROR] Docker recovery failed.
        exit /b 1
    )
)

echo [INFO] Starting compose stack...
docker compose up -d --remove-orphans
if errorlevel 1 (
    echo [WARN] docker compose up failed. Attempting one more Docker recovery...
    call :recoverDocker
    if errorlevel 1 exit /b 1

    docker compose up -d --remove-orphans
    if errorlevel 1 (
        echo [ERROR] docker compose up failed after recovery.
        exit /b 1
    )
)

call :waitHealth "Backend" "http://localhost:8800/health" 30 2
if errorlevel 1 exit /b 1
call :waitHealth "Prometheus" "http://localhost:9090/-/healthy" 30 2
if errorlevel 1 exit /b 1
call :waitHealth "Grafana" "http://localhost:3001/api/health" 30 2
if errorlevel 1 exit /b 1
call :waitHealth "Frontend" "http://localhost:3000" 30 2
if errorlevel 1 exit /b 1

echo [SUCCESS] Stack is up and healthy.
exit /b 0

:waitDocker
for /l %%i in (1,1,20) do (
    docker info >nul 2>&1
    if not errorlevel 1 (
        echo [INFO] Docker daemon is reachable.
        exit /b 0
    )
    echo [INFO] Waiting for Docker daemon ^(%%i/20^)...
    timeout /t 2 /nobreak >nul
)
exit /b 1

:recoverDocker
echo [INFO] Recovering Docker Desktop...
taskkill /F /IM "Docker Desktop.exe" /T >nul 2>&1
taskkill /F /IM "com.docker.backend.exe" /T >nul 2>&1
taskkill /F /IM "com.docker.build.exe" /T >nul 2>&1
wsl --shutdown >nul 2>&1
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"

for /l %%i in (1,1,60) do (
    docker info >nul 2>&1
    if not errorlevel 1 (
        echo [INFO] Docker recovered.
        exit /b 0
    )
    echo [INFO] Waiting for Docker recovery ^(%%i/60^)...
    timeout /t 2 /nobreak >nul
)

echo [ERROR] Docker daemon did not recover in time.
exit /b 1

:waitHealth
set "NAME=%~1"
set "URL=%~2"
set "RETRIES=%~3"
set "SLEEP=%~4"

for /l %%i in (1,1,%RETRIES%) do (
    curl -fsS "%URL%" >nul 2>&1
    if not errorlevel 1 (
        echo [INFO] %NAME% is healthy.
        exit /b 0
    )
    echo [INFO] Waiting for %NAME% health ^(%%i/%RETRIES%^)...
    timeout /t %SLEEP% /nobreak >nul
)

echo [ERROR] %NAME% did not become healthy: %URL%
exit /b 1
