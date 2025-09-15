# Chat App Deployment Guide for Render.com

## Prerequisites
- GitHub account
- Render.com account
- Git installed locally

## Step-by-Step Deployment Instructions

### 1. Prepare Your Repository

1. **Initialize Git repository** (if not already done):
   ```bash
   git init
   git add .
   git commit -m "Initial commit - Chat App ready for deployment"
   ```

2. **Create GitHub repository**:
   - Go to GitHub and create a new repository named `chat-app`
   - Push your code:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/chat-app.git
   git branch -M main
   git push -u origin main
   ```

### 2. Deploy Backend on Render

1. **Go to Render Dashboard**: https://dashboard.render.com/
2. **Click "New +"** → **"Web Service"**
3. **Connect your GitHub repository**
4. **Configure Backend Service**:
   - **Name**: `chat-app-backend`
   - **Region**: Choose closest to your users
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
   - **Build Command**: (leave empty - Docker handles this)
   - **Start Command**: (leave empty - Docker handles this)

5. **Environment Variables**: (Add these in Render dashboard)
   ```
   PORT=8080
   ```

6. **Deploy**: Click "Create Web Service"

### 3. Deploy Frontend on Render

1. **Click "New +"** → **"Web Service"**
2. **Connect the same GitHub repository**
3. **Configure Frontend Service**:
   - **Name**: `chat-app-frontend`
   - **Region**: Same as backend
   - **Branch**: `main`
   - **Root Directory**: (leave empty - uses root)
   - **Runtime**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`

4. **Environment Variables**:
   ```
   NODE_ENV=production
   ```

5. **Deploy**: Click "Create Web Service"

### 4. Update Frontend Configuration

1. **Get your backend URL** from Render (e.g., `https://chat-app-backend-xyz.onrender.com`)
2. **Update config.js** in frontend folder:
   ```javascript
   const ENV_CONFIG = {
       API_BASE_URL: window.location.hostname === 'localhost' 
           ? 'http://localhost:8080'
           : 'https://YOUR_ACTUAL_BACKEND_URL.onrender.com',
       
       WS_BASE_URL: window.location.hostname === 'localhost'
           ? 'http://localhost:8080/ws'
           : 'https://YOUR_ACTUAL_BACKEND_URL.onrender.com/ws'
   };
   ```
3. **Commit and push changes**:
   ```bash
   git add .
   git commit -m "Update API URLs for production"
   git push
   ```

### 5. Test Your Deployment

1. **Access your frontend**: Use the frontend URL from Render
2. **Test features**:
   - User registration/login
   - Private messaging
   - Group chats
   - Real-time communication

## Important Notes

- **Free Tier Limitations**: Render's free tier may have limitations
- **Cold Starts**: Services may take time to wake up after inactivity
- **WebSocket Support**: Ensure WebSocket connections work properly
- **CORS**: Already configured for production

## Monitoring

- Check Render logs for any deployment issues
- Monitor both frontend and backend services
- Test all chat functionality after deployment

## Troubleshooting

1. **Backend not starting**: Check Java version and dependencies
2. **Frontend not loading**: Verify Node.js version and build process
3. **API calls failing**: Check backend URL configuration
4. **WebSocket issues**: Verify WebSocket URL and CORS settings

Your chat application should now be fully deployed and accessible worldwide!