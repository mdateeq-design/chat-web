# 🚀 Chat Application - Port Setup Guide

## 📋 **Port Configuration Overview**

This chat application uses **TWO SEPARATE SERVERS** running on different ports:

### 🔧 **Backend Server (Spring Boot)**
- **Port**: `8080`
- **URL**: `http://localhost:8080`
- **Technology**: Java Spring Boot
- **Purpose**: 
  - Handles real-time chat messages via WebSocket
  - Manages user connections and chat history
  - Provides REST API endpoints
- **WebSocket Endpoint**: `ws://localhost:8080/ws`

### 🎨 **Frontend Server (Node.js)**
- **Port**: `3000`
- **URL**: `http://localhost:3000`
- **Technology**: Node.js HTTP Server
- **Purpose**:
  - Serves the chat user interface (HTML, CSS, JavaScript)
  - Provides the chat application frontend
  - Connects to backend via WebSocket

---

## 🚀 **How to Start the Application**

### **Option 1: Start Both Servers (Recommended)**
```bash
# Double-click this file:
start-both.bat
```
This will start both servers automatically in separate windows.

### **Option 2: Start Servers Individually**

#### Start Backend Only:
```bash
# Double-click this file:
start-backend.bat
```
Or manually:
```bash
cd backend
mvn spring-boot:run
```

#### Start Frontend Only:
```bash
# Double-click this file:
start-frontend.bat
```
Or manually:
```bash
node server.js
```

---

## 🌐 **Access Points**

| Service | URL | Purpose |
|---------|-----|---------|
| **Chat Application** | `http://localhost:3000/frontend/index.html` | Main chat interface |
| **Frontend Server** | `http://localhost:3000` | Frontend server home |
| **Backend API** | `http://localhost:8080` | Backend server home |
| **WebSocket** | `ws://localhost:8080/ws` | Real-time communication |

---

## 🔍 **How It Works**

1. **Frontend (Port 3000)**: Serves the chat UI and connects to backend
2. **Backend (Port 8080)**: Handles WebSocket connections and message processing
3. **Communication**: Frontend JavaScript connects to backend WebSocket for real-time chat

---

## ⚠️ **Important Notes**

- **Both servers must be running** for the chat to work properly
- **Start Backend first**, then Frontend (or use `start-both.bat`)
- **Port 8080** must be free for the backend
- **Port 3000** must be free for the frontend
- If you get port conflicts, kill existing processes using these ports

---

## 🛠️ **Troubleshooting**

### Port Already in Use Error:
```bash
# Find processes using ports
netstat -ano | findstr :8080
netstat -ano | findstr :3000

# Kill processes (replace PID with actual process ID)
taskkill /PID [PID_NUMBER] /F
```

### Backend Not Starting:
- Make sure you're in the `backend` directory
- Run `mvn clean` first if you have compilation issues
- Check that Java is installed and Maven is working

### Frontend Not Loading:
- Make sure Node.js is installed
- Check that `server.js` exists in the root directory
- Verify port 3000 is not being used by another application

---

## 📁 **File Structure**

```
chat-app/
├── backend/                 # Spring Boot Backend (Port 8080)
│   ├── src/main/java/
│   └── pom.xml
├── frontend/               # Frontend Files (Served on Port 3000)
│   ├── index.html
│   ├── app.js
│   └── style.css
├── server.js              # Frontend Server (Port 3000)
├── start-backend.bat      # Start backend only
├── start-frontend.bat     # Start frontend only
├── start-both.bat         # Start both servers
└── index.html             # Landing page (Port 8080)
```

---

## 🎯 **Quick Start Summary**

1. **Double-click `start-both.bat`** (easiest way)
2. **Wait for both servers to start** (you'll see "Started ChatApplication" for backend)
3. **Open browser** and go to `http://localhost:3000/frontend/index.html`
4. **Enter your username** and start chatting!

---

*Happy Chatting! 💬*




