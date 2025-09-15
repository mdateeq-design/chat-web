// Simple authentication handling
document.addEventListener('DOMContentLoaded', function() {
    // Check for logout query parameter to force clear
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('logout') === 'true') {
        localStorage.removeItem('user');
    }
    
    // Check if user is already logged in
    const user = localStorage.getItem('user');
    if (user) {
        window.location.href = 'index.html';
    }
    
    // Login form
    document.getElementById('login-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        
        try {
            console.log('Attempting login with:', { username, password: '***' });
            
            const response = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    usernameOrPhone: username,
                    password: password
                })
            });
            
            console.log('Login response status:', response.status);
            console.log('Login response headers:', response.headers);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            console.log('Login result:', result);
            
            if (result.success) {
                localStorage.setItem('user', JSON.stringify(result.user));
                showMessage('Login successful! Redirecting...', 'success');
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1000);
            } else {
                showMessage('Login failed: ' + result.message, 'error');
            }
        } catch (error) {
            console.error('Login error details:', error);
            showMessage('Login failed: ' + error.message, 'error');
        }
    });
    
    // Signup form
    document.getElementById('signup-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = document.getElementById('signup-username').value;
        const name = document.getElementById('signup-name').value;
        const phone = document.getElementById('signup-phone').value;
        const password = document.getElementById('signup-password').value;
        const confirmPassword = document.getElementById('signup-confirm-password').value;
        
        if (password !== confirmPassword) {
            showMessage('Passwords do not match!', 'error');
            return;
        }
        
        try {
            console.log('Attempting signup with:', { username, name, phone, password: '***' });
            
            const response = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/auth/signup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: username,
                    name: name,
                    phoneNumber: phone,
                    password: password
                })
            });
            
            console.log('Signup response status:', response.status);
            console.log('Signup response headers:', response.headers);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            console.log('Signup result:', result);
            
            if (result.success) {
                showMessage('Signup successful! Please login.', 'success');
                // Switch to login tab
                showTab('login');
                document.getElementById('login-username').value = username;
            } else {
                showMessage('Signup failed: ' + result.message, 'error');
            }
        } catch (error) {
            console.error('Signup error details:', error);
            showMessage('Signup failed: ' + error.message, 'error');
        }
    });
});

function showTab(tabName) {
    // Hide all forms
    document.querySelectorAll('.auth-form').forEach(form => {
        form.classList.remove('active');
    });
    
    // Remove active class from all tabs
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });
    
    // Show selected form and activate tab
    document.getElementById(tabName + '-form').classList.add('active');
    
    // Find and activate the correct tab button
    const tabButtons = document.querySelectorAll('.tab-button');
    if (tabName === 'login') {
        tabButtons[0].classList.add('active');
    } else if (tabName === 'signup') {
        tabButtons[1].classList.add('active');
    }
}

function showMessage(message, type) {
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');
    
    // Hide both messages first
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';
    
    if (type === 'error') {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    } else if (type === 'success') {
        successDiv.textContent = message;
        successDiv.style.display = 'block';
    }
}
