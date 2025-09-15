package com.chatapp.controller;

import com.chatapp.model.ChatRoom;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.service.ChatService;
import com.chatapp.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3000/frontend", "http://127.0.0.1:3000", "http://127.0.0.1:3000/frontend"}, allowCredentials = "true")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // WebSocket endpoints for real-time messaging
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> message) {
        String messageType = message.get("messageType").toString();
        String content = message.get("content").toString();
        String senderUsername = message.get("username").toString();
        
        // Create response message
        Map<String, Object> response = new HashMap<>();
        response.put("username", senderUsername);
        response.put("content", content);
        response.put("messageType", messageType);
        response.put("timestamp", System.currentTimeMillis());
        
        // Check if it's a private message
        if (message.containsKey("targetUser")) {
            String targetUser = message.get("targetUser").toString();
            response.put("targetUser", targetUser);
            response.put("isPrivate", true);
            
            // Send to both sender and recipient
            messagingTemplate.convertAndSendToUser(senderUsername, "/queue/private", response);
            messagingTemplate.convertAndSendToUser(targetUser, "/queue/private", response);
        }
        // Check if it's a group message
        else if (message.containsKey("chatRoomId")) {
            Long chatRoomId = Long.valueOf(message.get("chatRoomId").toString());
            response.put("chatRoomId", chatRoomId);
            response.put("isGroup", true);
            
            // Send to group topic
            messagingTemplate.convertAndSend("/topic/group/" + chatRoomId, response);
        }
        // Default to public message
        else {
            messagingTemplate.convertAndSend("/topic/public", response);
        }
    }
    
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload Map<String, Object> payload) {
        String username = payload.get("username").toString();
        onlineUsers.add(username);
        
        // Send join message to public
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("content", username + " joined the chat");
        response.put("messageType", "JOIN");
        response.put("timestamp", System.currentTimeMillis());
        response.put("onlineCount", onlineUsers.size());
        
        messagingTemplate.convertAndSend("/topic/public", response);
    }
    
    // REST endpoints
    @PostMapping("/private/{userId2}")
    public ResponseEntity<?> createPrivateChat(@PathVariable Long userId2, @RequestHeader("User-Id") Long userId1) {
        try {
            ChatRoom chatRoom = chatService.createOrGetPrivateChat(userId1, userId2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chatRoom", createChatRoomResponse(chatRoom));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/group")
    public ResponseEntity<?> createGroupChat(@RequestBody CreateGroupRequest request, @RequestHeader("User-Id") Long userId) {
        try {
            ChatRoom chatRoom = chatService.createGroupChat(
                request.getName(),
                request.getDescription(),
                userId,
                request.getParticipantIds()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chatRoom", createChatRoomResponse(chatRoom));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/rooms")
    public ResponseEntity<?> getUserChatRooms(@RequestHeader("User-Id") Long userId) {
        try {
            List<ChatRoom> chatRooms = chatService.getUserChatRooms(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chatRooms", chatRooms.stream().map(this::createChatRoomResponse).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> getChatMessages(@PathVariable Long chatRoomId, 
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size,
                                           @RequestHeader("User-Id") Long userId) {
        try {
            List<Message> messages = chatService.getChatMessages(chatRoomId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages.stream().map(this::createMessageResponse).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/rooms/{chatRoomId}/participants/{participantId}")
    public ResponseEntity<?> addParticipantToGroup(@PathVariable Long chatRoomId,
                                                  @PathVariable Long participantId,
                                                  @RequestHeader("User-Id") Long userId) {
        try {
            ChatRoom chatRoom = chatService.addParticipantToGroupChat(chatRoomId, participantId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chatRoom", createChatRoomResponse(chatRoom));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Contact management endpoints
    @PostMapping("/contacts/phone/{phoneNumber}")
    public ResponseEntity<?> addContactByPhone(@PathVariable String phoneNumber, @RequestHeader("User-Id") Long userId) {
        try {
            User contact = contactService.addContactByPhone(userId, phoneNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", createUserResponse(contact));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/contacts/{contactId}")
    public ResponseEntity<?> removeContact(@PathVariable Long contactId, @RequestHeader("User-Id") Long userId) {
        try {
            contactService.removeContact(userId, contactId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Contact removed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/contacts")
    public ResponseEntity<?> getUserContacts(@RequestHeader("User-Id") Long userId) {
        try {
            Set<User> contacts = contactService.getUserContacts(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts.stream().map(this::createUserResponse).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/search/users")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            List<User> users = contactService.searchUsers(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users.stream().map(this::createUserResponse).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Simple online users tracking (in-memory for demo)
    private static final Set<String> onlineUsers = java.util.concurrent.ConcurrentHashMap.newKeySet();
    
    @MessageMapping("/chat.userOnline")
    public void userOnline(@Payload Map<String, Object> payload) {
        String username = payload.get("username").toString();
        onlineUsers.add(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("content", username + " is now online");
        response.put("messageType", "ONLINE");
        response.put("timestamp", System.currentTimeMillis());
        response.put("onlineCount", onlineUsers.size());
        response.put("onlineUsers", new java.util.ArrayList<>(onlineUsers));
        
        messagingTemplate.convertAndSend("/topic/public", response);
    }
    
    @MessageMapping("/chat.userOffline")
    public void userOffline(@Payload Map<String, Object> payload) {
        String username = payload.get("username").toString();
        onlineUsers.remove(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("content", username + " went offline");
        response.put("messageType", "OFFLINE");
        response.put("timestamp", System.currentTimeMillis());
        response.put("onlineCount", onlineUsers.size());
        response.put("onlineUsers", new java.util.ArrayList<>(onlineUsers));
        
        messagingTemplate.convertAndSend("/topic/public", response);
    }
    
    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("onlineUsers", new java.util.ArrayList<>(onlineUsers));
        response.put("count", onlineUsers.size());
        
        return ResponseEntity.ok(response);
    }
    
    // Helper methods to create response objects
    private Map<String, Object> createChatRoomResponse(ChatRoom chatRoom) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", chatRoom.getId());
        response.put("name", chatRoom.getName());
        response.put("type", chatRoom.getType());
        response.put("description", chatRoom.getDescription());
        response.put("createdAt", chatRoom.getCreatedAt());
        response.put("participants", chatRoom.getParticipants().stream().map(this::createUserResponse).toList());
        return response;
    }
    
    private Map<String, Object> createMessageResponse(Message message) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("content", message.getContent());
        response.put("messageType", message.getMessageType());
        response.put("timestamp", message.getTimestamp());
        response.put("sender", createUserResponse(message.getSender()));
        return response;
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("name", user.getName());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("isOnline", user.isOnline());
        response.put("lastSeen", user.getLastSeen());
        return response;
    }
    
    // Request DTOs
    public static class CreateGroupRequest {
        private String name;
        private String description;
        private List<Long> participantIds;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<Long> getParticipantIds() { return participantIds; }
        public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
    }
}