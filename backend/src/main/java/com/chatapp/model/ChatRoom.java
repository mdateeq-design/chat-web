package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type; // PRIVATE, GROUP
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "chat_room_participants",
        joinColumns = @JoinColumn(name = "chat_room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();
    
    @Column
    private String description;
    
    @Column
    private String avatar; // For group chats
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    public enum ChatType {
        PRIVATE, GROUP
    }
    
    // Constructors
    public ChatRoom() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ChatRoom(String name, ChatType type, User createdBy) {
        this();
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public ChatType getType() { return type; }
    public void setType(ChatType type) { this.type = type; }
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public Set<User> getParticipants() { return participants; }
    public void setParticipants(Set<User> participants) { this.participants = participants; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public void addParticipant(User user) {
        this.participants.add(user);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeParticipant(User user) {
        this.participants.remove(user);
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean hasParticipant(User user) {
        return this.participants.contains(user);
    }
}
