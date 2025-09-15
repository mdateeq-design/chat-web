// Environment configuration
const ENV_CONFIG = {
    // Backend API URL - will be set based on environment
    API_BASE_URL: window.location.hostname === 'localhost' 
        ? 'http://localhost:8080'
        : 'https://YOUR_BACKEND_URL.onrender.com',
    
    // WebSocket URL - will be set based on environment
    WS_BASE_URL: window.location.hostname === 'localhost'
        ? 'http://localhost:8080/ws'
        : 'https://YOUR_BACKEND_URL.onrender.com/ws'
};

// Export for use in other files
window.ENV_CONFIG = ENV_CONFIG;