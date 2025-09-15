@echo off
echo ============================================
echo   CHAT APPLICATION PRODUCTION SETUP
echo ============================================
echo.
echo Starting both Frontend and Backend servers...
echo.
echo FRONTEND (Express + Node.js):
echo - URL: http://localhost:3000
echo - Purpose: Serves chat UI files with CORS
echo.
echo BACKEND (Spring Boot):
echo - URL: http://localhost:8080
echo - Purpose: Handles WebSocket connections and API
echo - WebSocket: ws://localhost:8080/ws
echo.
echo CHAT APP:
echo - URL: http://localhost:3000
echo.
echo ============================================
echo.
echo Step 1: Starting Backend Server (Port 8080)...
start "Backend Server - Port 8080" cmd /k "cd backend && echo Backend starting on port 8080... && mvn spring-boot:run"
echo Backend starting in separate window...
timeout /t 10 /nobreak > nul
echo.
echo Step 2: Starting Frontend Server (Port 3000)...
start "Frontend Server - Port 3000" cmd /k "echo Frontend starting on port 3000... && npm start"
echo Frontend starting in separate window...
echo.
echo ============================================
echo   SERVERS STARTING...
echo ============================================
echo.
echo Wait for both servers to fully load, then:
echo 1. Backend: Look for "Started ChatApplication" message
echo 2. Frontend: Look for "Frontend server running" message
echo 3. Open: http://localhost:3000
echo.
echo Ready for production deployment testing!
echo Press any key to continue...
pause > nul
