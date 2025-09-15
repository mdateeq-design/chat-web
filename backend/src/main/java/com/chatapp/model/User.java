package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    
    @Column(nullable = false)
    private String password; // In production, this should be encrypted
    
    @Column
    private String profilePicture;
    
    @Column
    private boolean isOnline = false;
    
    @Column
    private LocalDateTime lastSeen;
    
    @Column
    private LocalDateTime createdAt;
    
    // Many-to-many relationship for contacts/friends
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_contacts",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<User> contacts = new HashSet<>();
    
    // Constructor
    public User() {
        this.createdAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }
    
    public User(String username, String name, String phoneNumber, String password) {
        this();
        this.username = username;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { 
        this.isOnline = online;
        if (!online) {
            this.lastSeen = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Set<User> getContacts() { return contacts; }
    public void setContacts(Set<User> contacts) { this.contacts = contacts; }
    
    // Helper methods
    public void addContact(User contact) {
        this.contacts.add(contact);
        contact.getContacts().add(this); // Bidirectional relationship
    }
    
    public void removeContact(User contact) {
        this.contacts.remove(contact);
        contact.getContacts().remove(this);
    }
}
