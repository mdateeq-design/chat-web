# Real-Time Chat Application

A modern real-time chat application built with Spring Boot backend and vanilla JavaScript frontend.

## Features

- Real-time messaging using WebSockets
- User authentication with username
- Online user list
- Typing indicators
- Modern, responsive UI
- Cross-browser compatibility

## Quick Start

### 1. Start the Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Wait for the message: `Started ChatApplication`

### 2. Start the Frontend Server

```bash
# In the root directory
node server.js
```

Or if you have npm:
```bash
npm start
```

### 3. Open the Application

Open your browser and go to: `http://localhost:3000`

## How to Use

1. **Start Backend**: Make sure the Spring Boot backend is running on port 8080
2. **Start Frontend**: Run the Node.js server on port 3000
3. **Open Chat**: Click "Open Chat App" on the homepage
4. **Enter Username**: Enter your username when prompted
5. **Start Chatting**: Send messages and see them appear in real-time!

## Project Structure

```
chat-app/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/      # Java source code
│   └── pom.xml            # Maven configuration
├── frontend/               # Frontend files
│   ├── index.html         # Chat application UI
│   ├── app.js            # JavaScript logic
│   └── style.css         # Styling
├── index.html             # Landing page
├── server.js             # Node.js server for frontend
└── package.json          # Node.js dependencies
```

## Technology Stack

### Backend
- Java 11+
- Spring Boot 2.7+
- Spring WebSocket
- Maven

### Frontend
- Vanilla JavaScript
- HTML5/CSS3
- SockJS
- STOMP protocol

## Troubleshooting

### Backend Issues
- Make sure Java 11+ is installed
- Ensure port 8080 is not in use
- Check Maven is installed and working

### Frontend Issues
- Make sure Node.js is installed
- Ensure port 3000 is not in use
- Check browser console for errors

### Connection Issues
- Verify backend is running on port 8080
- Check firewall settings
- Ensure both servers are running simultaneously

## Development

To modify the application:

1. **Backend changes**: Edit Java files in `backend/src/main/java/`
2. **Frontend changes**: Edit files in `frontend/` directory
3. **Restart servers** after making changes

## License

MIT License



