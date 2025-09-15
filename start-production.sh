#!/bin/bash

echo "🚀 Starting Chat App for Production Testing..."
echo ""

# Start backend in background
echo "📡 Starting Backend Server..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# Wait a bit for backend to start
sleep 10

# Start frontend
echo "🌐 Starting Frontend Server..."
cd ..
npm start &
FRONTEND_PID=$!

echo ""
echo "✅ Both servers started!"
echo "📡 Backend: http://localhost:8080"
echo "🌐 Frontend: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop both servers"

# Handle Ctrl+C
trap 'echo "🛑 Stopping servers..."; kill $BACKEND_PID $FRONTEND_PID; exit' INT

# Wait for user input
wait