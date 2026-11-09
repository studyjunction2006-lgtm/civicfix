# CivicFix - PowerShell Launcher
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  CivicFix - Community Infrastructure Platform Launcher" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$workspace = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "[1/3] Starting Spring Boot Backend (Port 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$workspace\backend'; mvn spring-boot:run"

Write-Host "[2/3] Starting Vite React Frontend (Port 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$workspace\frontend'; cmd.exe /c npm run dev"

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "  CivicFix services are launching!" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host "  Frontend:   http://localhost:5173" -ForegroundColor White
Write-Host "  Backend:    http://localhost:8080" -ForegroundColor White
Write-Host "  Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  H2 Console: http://localhost:8080/h2-console" -ForegroundColor White
Write-Host ""
Write-Host "Pre-configured Demo Accounts (Password: password123):" -ForegroundColor Magenta
Write-Host "  - Admin / Dean:      admin@civicfix.org" -ForegroundColor Gray
Write-Host "  - Authority / Head:  authority@civicfix.org" -ForegroundColor Gray
Write-Host "  - Field Worker:      worker@civicfix.org" -ForegroundColor Gray
Write-Host "  - Citizen / Student: citizen@civicfix.org" -ForegroundColor Gray
Write-Host ""
Write-Host "Waiting 8 seconds before launching browser..." -ForegroundColor DarkGray
Start-Sleep -Seconds 8
Start-Process "http://localhost:5173"
