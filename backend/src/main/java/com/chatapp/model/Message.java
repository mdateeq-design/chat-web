package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private String messageType = "CHAT"; // CHAT, JOIN, LEAVE, SYSTEM
    
    // Constructors
    public Message() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(User sender, ChatRoom chatRoom, String content, String messageType) {
        this();
        this.sender = sender;
        this.chatRoom = chatRoom;
        this.content = content;
        this.messageType = messageType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    
    public ChatRoom getChatRoom() { return chatRoom; }
    public void setChatRoom(ChatRoom chatRoom) { this.chatRoom = chatRoom; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    // Helper method for backward compatibility
    public String getUsername() {
        return sender != null ? sender.getUsername() : null;
    }
}
