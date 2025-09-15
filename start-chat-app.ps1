Write-Host "Starting Chat Application..." -ForegroundColor Green
Set-Location -Path "backend"
mvn spring-boot:run
Read-Host "Press Enter to continue"
