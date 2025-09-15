const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all routes
app.use(cors());

// Serve static files from frontend directory
app.use(express.static(path.join(__dirname, 'frontend')));

// Serve static files from root directory
app.use(express.static(__dirname));

// Route for the main page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'index.html'));
});

// Route for auth page
app.get('/auth', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'auth.html'));
});

// Catch all other routes and redirect to main page
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`Frontend server running at http://localhost:${PORT}/`);
    console.log('Press Ctrl+C to stop the server');
});


