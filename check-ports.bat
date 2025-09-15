@echo off
echo ============================================
echo   PORT STATUS CHECKER
echo ============================================
echo.
echo Checking which ports are in use...
echo.

echo Frontend Port (3000):
netstat -ano | findstr :3000
if %errorlevel% == 0 (
    echo ✓ Port 3000 is IN USE - Frontend is running
) else (
    echo ✗ Port 3000 is FREE - Frontend is NOT running
)
echo.

echo Backend Port (8080):
netstat -ano | findstr :8080
if %errorlevel% == 0 (
    echo ✓ Port 8080 is IN USE - Backend is running
) else (
    echo ✗ Port 8080 is FREE - Backend is NOT running
)
echo.

echo ============================================
echo   QUICK TESTS
echo ============================================
echo.
echo Testing Backend (Port 8080):
curl -s http://localhost:8080/test 2>nul
if %errorlevel% == 0 (
    echo ✓ Backend is responding
) else (
    echo ✗ Backend is not responding
)
echo.

echo Testing Frontend (Port 3000):
curl -s http://localhost:3000 2>nul
if %errorlevel% == 0 (
    echo ✓ Frontend is responding
) else (
    echo ✗ Frontend is not responding
)
echo.

echo ============================================
echo   ACCESS POINTS
echo ============================================
echo.
echo If both servers are running:
echo - Chat App: http://localhost:3000/frontend/index.html
echo - Backend API: http://localhost:8080
echo - Frontend Server: http://localhost:3000
echo.
pause


