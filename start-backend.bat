@echo off
echo Starting Backend Server on Port 8080...
echo.
echo Backend API will be available at: http://localhost:8080
echo WebSocket endpoint: ws://localhost:8080/ws
echo.
cd backend
mvn spring-boot:run
pause
