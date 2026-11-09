@echo off
title CivicFix - Local Development Launcher
echo ========================================================
echo   CivicFix - Community Infrastructure Platform Launcher
echo ========================================================
echo.
echo [1/3] Verifying environment...
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven (mvn) was not found in your PATH.
    echo Please install Maven or add it to PATH.
    pause
    exit /b 1
)

where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Node.js was not found in your PATH.
    echo Please install Node.js 18+.
    pause
    exit /b 1
)

echo [OK] Java/Maven and Node.js detected.
echo.
echo [2/3] Starting Spring Boot Backend (Port 8080)...
start "CivicFix Backend (Spring Boot 8080)" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo [3/3] Starting Vite Frontend (Port 5173)...
start "CivicFix Frontend (Vite 5173)" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo ========================================================
echo   CivicFix is starting up!
echo ========================================================
echo   Frontend:   http://localhost:5173
echo   Backend:    http://localhost:8080
echo   Swagger UI: http://localhost:8080/swagger-ui.html
echo   H2 Console: http://localhost:8080/h2-console
echo.
echo Pre-configured Demo Accounts (Password: password123):
echo   - Admin / Dean:      admin@civicfix.org
echo   - Authority / Head:  authority@civicfix.org
echo   - Field Worker:      worker@civicfix.org
echo   - Citizen / Student: citizen@civicfix.org
echo.
echo Tip: Use the 1-click Quick Demo Switcher bar at the top
echo of the web app to switch between personas instantly!
echo ========================================================
echo.
echo Waiting 8 seconds for services to initialize, then launching browser...
timeout /t 8 /nobreak >nul
start http://localhost:5173
