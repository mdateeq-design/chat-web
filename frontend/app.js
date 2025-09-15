class ChatApp {
    constructor() {
        this.socket = null;
        this.username = '';
        this.user = null;
        this.isConnected = false;
        this.typingTimer = null;
        this.isTyping = false;
        this.currentChatType = 'public'; // 'public', 'private', 'group'
        this.currentChatTarget = null; // username for private, chatRoomId for group
        this.onlineUsers = [];
        this.contacts = [];
        this.chatRooms = [];
        
        this.initializeElements();
        this.attachEventListeners();
        this.checkAuthentication();
    }

    initializeElements() {
        // Get DOM elements
        this.authModal = document.getElementById('auth-modal');
        this.usernameDisplay = document.getElementById('username-display');
        this.messagesContainer = document.getElementById('messages-container');
        this.messageForm = document.getElementById('message-form');
        this.messageInput = document.getElementById('message-input');
        this.typingIndicator = document.getElementById('typing-indicator');
        this.userCount = document.getElementById('user-count');
        this.usersList = document.getElementById('users-list');
        this.logoutBtn = document.getElementById('logout-btn');
        this.switchUserBtn = document.getElementById('switch-user-btn');
        
        // Chat selection elements
        this.contactsList = document.getElementById('contacts-list');
        this.chatsList = document.getElementById('chats-list');
        this.groupsList = document.getElementById('groups-list');
        this.contactSearch = document.getElementById('contact-search');
        this.searchUsersBtn = document.getElementById('search-users-btn');
        this.createGroupBtn = document.getElementById('create-group-btn');
    }

    attachEventListeners() {
        // Logout button
        this.logoutBtn.addEventListener('click', () => {
            this.handleLogout();
        });

        // Switch user button
        this.switchUserBtn.addEventListener('click', () => {
            this.handleSwitchUser();
        });

        // Search users functionality
        this.searchUsersBtn.addEventListener('click', () => {
            this.searchUsers();
        });
        
        this.contactSearch.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.searchUsers();
            }
        });
        
        // Create group functionality
        this.createGroupBtn.addEventListener('click', () => {
            this.showCreateGroupModal();
        });

        // Message form
        this.messageForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.sendMessage();
        });

        // Typing indicators
        this.messageInput.addEventListener('input', () => {
            this.handleTyping();
        });

        this.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Handle browser close/refresh
        window.addEventListener('beforeunload', () => {
            if (this.stompClient && this.stompClient.connected) {
                this.stompClient.send("/app/chat.userOffline", {}, JSON.stringify({
                    username: this.username
                }));
            }
        });
    }

    checkAuthentication() {
        // Check if user is authenticated
        const userData = localStorage.getItem('user');
        if (userData) {
            try {
                this.user = JSON.parse(userData);
                this.username = this.user.username;
                this.usernameDisplay.textContent = this.user.name || this.user.username;
                this.hideAuthModal();
                this.connectToServer();
            } catch (error) {
                console.error('Error parsing user data:', error);
                this.showAuthModal();
            }
        } else {
            this.showAuthModal();
        }
    }

    showAuthModal() {
        this.authModal.style.display = 'flex';
    }

    hideAuthModal() {
        this.authModal.style.display = 'none';
    }

    handleLogout() {
        // Send offline message before disconnecting
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send("/app/chat.userOffline", {}, JSON.stringify({
                username: this.username
            }));
        }
        
        if (this.user && this.user.id) {
            // Call logout API
            fetch(`${ENV_CONFIG.API_BASE_URL}/api/auth/logout/${this.user.id}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            }).catch(error => {
                console.error('Logout API error:', error);
            });
        }
        
        // Clear local storage and redirect
        localStorage.removeItem('user');
        window.location.href = 'auth.html';
    }

    handleSwitchUser() {
        // Send offline message before disconnecting
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send("/app/chat.userOffline", {}, JSON.stringify({
                username: this.username
            }));
        }
        
        // Clear local storage and redirect with logout parameter
        localStorage.removeItem('user');
        window.location.href = 'auth.html?logout=true';
    }

    connectToServer() {
        // Connect to the actual WebSocket backend
        this.connectWebSocket();
        
        // Show connection status
        this.addSystemMessage(`Connected as ${this.username}`);
        this.isConnected = true;
    }

    connectWebSocket() {
        // Connect to the Spring Boot WebSocket endpoint
        const socket = new SockJS(ENV_CONFIG.WS_BASE_URL);
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            this.addSystemMessage('Connected to chat server');
            
            // Subscribe to public messages
            this.stompClient.subscribe('/topic/public', (message) => {
                const receivedMessage = JSON.parse(message.body);
                this.receiveMessage(receivedMessage);
            });
            
            // Subscribe to private messages
            this.stompClient.subscribe('/user/queue/private', (message) => {
                const receivedMessage = JSON.parse(message.body);
                this.receivePrivateMessage(receivedMessage);
            });
            
            // Load user's chat data
            this.loadUserData();
            
            // Send user join message
            this.stompClient.send("/app/chat.addUser", {}, JSON.stringify({
                username: this.username
            }));
            
            // Send user online message
            this.stompClient.send("/app/chat.userOnline", {}, JSON.stringify({
                username: this.username
            }));
            
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.addSystemMessage('Failed to connect to chat server');
        });
    }


    sendMessage() {
        const messageText = this.messageInput.value.trim();
        if (!messageText || !this.isConnected) return;

        let messagePayload = {
            username: this.username,
            content: messageText,
            messageType: 'CHAT'
        };
        
        // Add target information based on current chat type
        if (this.currentChatType === 'private') {
            messagePayload.targetUser = this.currentChatTarget;
        } else if (this.currentChatType === 'group') {
            messagePayload.chatRoomId = this.currentChatTarget;
        }

        // Display own message immediately (except for group messages that need to come from server)
        if (this.currentChatType !== 'group') {
            this.displayMessage({
                username: this.username,
                content: messageText,
                messageType: 'CHAT',
                timestamp: Date.now()
            }, true);
        }
        
        // Clear input
        this.messageInput.value = '';
        
        // Stop typing indicator
        this.stopTyping();
        
        // Send to server
        this.sendToServer(messagePayload);
    }

    sendToServer(message) {
        if (this.stompClient && this.stompClient.connected) {
            // Send via WebSocket
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
        } else {
            // Fallback: just log it
            console.log('Sending message to server:', message);
        }
    }

    receiveMessage(message) {
        // Handle public messages and system messages
        if (message.messageType === 'ONLINE' || message.messageType === 'OFFLINE') {
            this.updateOnlineUsers(message.onlineUsers || []);
        }
        this.displayMessage(message, false);
    }
    
    receivePrivateMessage(message) {
        // Handle private messages
        this.displayMessage(message, message.username === this.username);
    }

    displayMessage(message, isOwnMessage) {
        const messageElement = document.createElement('div');
        
        // Check if it's a system message (JOIN/LEAVE/ONLINE/OFFLINE)
        if (message.messageType === 'JOIN' || message.messageType === 'LEAVE' || 
            message.messageType === 'ONLINE' || message.messageType === 'OFFLINE') {
            messageElement.className = 'system-message';
            messageElement.textContent = message.content;
            
            // Update user count if available
            if (message.onlineCount !== undefined) {
                this.updateUserCount(message.onlineCount);
            }
        } else {
            // Regular chat message
            messageElement.className = `message ${isOwnMessage ? 'own' : 'other'}`;
            
            const timeString = new Date(message.timestamp || new Date()).toLocaleTimeString([], {
                hour: '2-digit',
                minute: '2-digit'
            });
            
            messageElement.innerHTML = `
                <div class="message-info">
                    ${isOwnMessage ? 'You' : message.username} • ${timeString}
                </div>
                <div class="message-text">${this.escapeHtml(message.content)}</div>
            `;
        }
        
        this.messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
    }

    addSystemMessage(text) {
        const systemElement = document.createElement('div');
        systemElement.className = 'system-message';
        systemElement.textContent = text;
        this.messagesContainer.appendChild(systemElement);
        this.scrollToBottom();
    }

    handleTyping() {
        if (!this.isTyping) {
            this.isTyping = true;
            // Send typing start event to server
            console.log('User started typing');
        }

        // Clear existing timer
        clearTimeout(this.typingTimer);
        
        // Set new timer
        this.typingTimer = setTimeout(() => {
            this.stopTyping();
        }, 1000);
    }

    stopTyping() {
        if (this.isTyping) {
            this.isTyping = false;
            clearTimeout(this.typingTimer);
            // Send typing stop event to server
            console.log('User stopped typing');
        }
    }

    updateUserCount(count) {
        this.userCount.textContent = count;
    }

    scrollToBottom() {
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    // New methods for private messaging and group chat
    async loadUserData() {
        if (!this.user || !this.user.id) return;
        
        try {
            // Load contacts
            const contactsResponse = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/chat/contacts`, {
                headers: {
                    'User-Id': this.user.id
                }
            });
            
            if (contactsResponse.ok) {
                const contactsResult = await contactsResponse.json();
                this.contacts = contactsResult.contacts || [];
                this.displayContacts();
            }
            
            // Load chat rooms
            const roomsResponse = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/chat/rooms`, {
                headers: {
                    'User-Id': this.user.id
                }
            });
            
            if (roomsResponse.ok) {
                const roomsResult = await roomsResponse.json();
                this.chatRooms = roomsResult.chatRooms || [];
                this.displayChatRooms();
            }
        } catch (error) {
            console.error('Error loading user data:', error);
        }
    }
    
    async searchUsers() {
        const query = this.contactSearch.value.trim();
        if (!query) return;
        
        try {
            const response = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/chat/search/users?query=${encodeURIComponent(query)}`);
            if (response.ok) {
                const result = await response.json();
                this.displaySearchResults(result.users || []);
            }
        } catch (error) {
            console.error('Error searching users:', error);
        }
    }
    
    displayContacts() {
        this.contactsList.innerHTML = '';
        
        this.contacts.forEach(contact => {
            const contactElement = document.createElement('div');
            contactElement.className = 'contact-item';
            contactElement.innerHTML = `
                <div class="contact-info">
                    <div class="contact-name">${contact.name}</div>
                    <div class="contact-username">@${contact.username}</div>
                </div>
                <button class="btn-chat" onclick="chatApp.startPrivateChat('${contact.username}')">
                    💬 Chat
                </button>
            `;
            this.contactsList.appendChild(contactElement);
        });
    }
    
    displaySearchResults(users) {
        this.contactsList.innerHTML = '<h4>Search Results:</h4>';
        
        users.forEach(user => {
            if (user.username === this.username) return; // Skip self
            
            const userElement = document.createElement('div');
            userElement.className = 'contact-item';
            userElement.innerHTML = `
                <div class="contact-info">
                    <div class="contact-name">${user.name}</div>
                    <div class="contact-username">@${user.username} • ${user.phoneNumber}</div>
                </div>
                <div class="contact-actions">
                    <button class="btn-add" onclick="chatApp.addContact('${user.phoneNumber}')">
                        ➕ Add
                    </button>
                    <button class="btn-chat" onclick="chatApp.startPrivateChat('${user.username}')">
                        💬 Chat
                    </button>
                </div>
            `;
            this.contactsList.appendChild(userElement);
        });
    }
    
    startPrivateChat(targetUsername) {
        this.currentChatType = 'private';
        this.currentChatTarget = targetUsername;
        
        // Clear messages and show private chat header
        this.messagesContainer.innerHTML = '';
        this.addSystemMessage(`Private chat with ${targetUsername}`);
        
        // Update message input placeholder
        this.messageInput.placeholder = `Message ${targetUsername}...`;
        
        console.log(`Started private chat with ${targetUsername}`);
    }
    
    async addContact(phoneNumber) {
        if (!this.user || !this.user.id) return;
        
        try {
            const response = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/chat/contacts/phone/${phoneNumber}`, {
                method: 'POST',
                headers: {
                    'User-Id': this.user.id
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                console.log('Contact added:', result);
                // Reload contacts
                this.loadUserData();
            } else {
                console.error('Failed to add contact');
            }
        } catch (error) {
            console.error('Error adding contact:', error);
        }
    }
    
    displayChatRooms() {
        // Display private chats
        this.chatsList.innerHTML = '<h4>Private Chats:</h4>';
        const privateChats = this.chatRooms.filter(room => room.type === 'PRIVATE');
        
        privateChats.forEach(room => {
            const chatElement = document.createElement('div');
            chatElement.className = 'chat-item';
            chatElement.innerHTML = `
                <div class="chat-info">
                    <div class="chat-name">${room.name}</div>
                    <div class="chat-type">Private Chat</div>
                </div>
                <button class="btn-open" onclick="chatApp.openChatRoom(${room.id}, 'private')">
                    Open
                </button>
            `;
            this.chatsList.appendChild(chatElement);
        });
        
        // Display group chats
        this.groupsList.innerHTML = '';
        const groupChats = this.chatRooms.filter(room => room.type === 'GROUP');
        
        groupChats.forEach(room => {
            const groupElement = document.createElement('div');
            groupElement.className = 'group-item';
            groupElement.innerHTML = `
                <div class="group-info">
                    <div class="group-name">${room.name}</div>
                    <div class="group-description">${room.description || ''}</div>
                    <div class="group-participants">${room.participants.length} members</div>
                </div>
                <button class="btn-open" onclick="chatApp.openChatRoom(${room.id}, 'group')">
                    Open
                </button>
            `;
            this.groupsList.appendChild(groupElement);
        });
    }
    
    openChatRoom(chatRoomId, type) {
        this.currentChatType = type;
        this.currentChatTarget = chatRoomId;
        
        // Clear messages
        this.messagesContainer.innerHTML = '';
        
        // Subscribe to group messages if it's a group chat
        if (type === 'group') {
            this.stompClient.subscribe(`/topic/group/${chatRoomId}`, (message) => {
                const receivedMessage = JSON.parse(message.body);
                this.displayMessage(receivedMessage, receivedMessage.username === this.username);
            });
            
            const room = this.chatRooms.find(r => r.id === chatRoomId);
            this.addSystemMessage(`Opened group: ${room ? room.name : 'Unknown Group'}`);
            this.messageInput.placeholder = `Message group...`;
        } else {
            const room = this.chatRooms.find(r => r.id === chatRoomId);
            this.addSystemMessage(`Opened chat: ${room ? room.name : 'Unknown Chat'}`);
            this.messageInput.placeholder = `Type your message...`;
        }
        
        // Load chat history
        this.loadChatHistory(chatRoomId);
    }
    
    async loadChatHistory(chatRoomId) {
        try {
            const response = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/chat/rooms/${chatRoomId}/messages`, {
                headers: {
                    'User-Id': this.user.id
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                const messages = result.messages || [];
                
                messages.forEach(msg => {
                    this.displayMessage({
                        username: msg.sender.username,
                        content: msg.content,
                        messageType: msg.messageType,
                        timestamp: new Date(msg.timestamp).getTime()
                    }, msg.sender.username === this.username);
                });
            }
        } catch (error) {
            console.error('Error loading chat history:', error);
        }
    }
    
    updateOnlineUsers(users) {
        this.onlineUsers = users || [];
        this.updateUserCount(this.onlineUsers.length);
        
        // Update users list display
        this.usersList.innerHTML = '';
        this.onlineUsers.forEach(username => {
            const userElement = document.createElement('li');
            userElement.textContent = username;
            if (username === this.username) {
                userElement.style.fontWeight = 'bold';
                userElement.style.color = '#667eea';
            }
            this.usersList.appendChild(userElement);
        });
    }
    
    showCreateGroupModal() {
        // Simple implementation for now - can be enhanced with a proper modal
        const groupName = prompt('Enter group name:');
        if (!groupName) return;
        
        const groupDescription = prompt('Enter group description (optional):') || '';
        
        this.createGroup(groupName, groupDescription, []);
    }
    
    async createGroup(name, description, participantIds) {
        if (!this.user || !this.user.id) return;
        
        try {
            const response = await fetch(`${ENV_CONFIG.API_BASE_URL}/api/chat/group`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'User-Id': this.user.id
                },
                body: JSON.stringify({
                    name: name,
                    description: description,
                    participantIds: participantIds
                })
            });
            
            if (response.ok) {
                const result = await response.json();
                console.log('Group created:', result);
                // Reload chat rooms
                this.loadUserData();
            } else {
                console.error('Failed to create group');
            }
        } catch (error) {
            console.error('Error creating group:', error);
        }
    }
}

// Initialize the chat app when page loads
let chatApp;
document.addEventListener('DOMContentLoaded', () => {
    chatApp = new ChatApp();
});

// Global functions for tab switching and UI interactions
function showChatTab(tabName) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from all tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tabName + '-tab').classList.add('active');
    
    // Activate the clicked tab button
    event.target.classList.add('active');
}
