package com.chatapp.service;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ContactService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Add contact by phone number
    public User addContactByPhone(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId).orElse(null);
        User contactToAdd = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        if (contactToAdd == null) {
            throw new RuntimeException("User with phone number not found");
        }
        
        if (user.getId().equals(contactToAdd.getId())) {
            throw new RuntimeException("Cannot add yourself as contact");
        }
        
        // Check if already a contact
        if (user.getContacts().contains(contactToAdd)) {
            throw new RuntimeException("User is already a contact");
        }
        
        // Add bidirectional contact relationship
        user.addContact(contactToAdd);
        userRepository.save(user);
        userRepository.save(contactToAdd);
        
        return contactToAdd;
    }
    
    // Remove contact
    public void removeContact(Long userId, Long contactId) {
        User user = userRepository.findById(userId).orElse(null);
        User contactToRemove = userRepository.findById(contactId).orElse(null);
        
        if (user == null || contactToRemove == null) {
            throw new RuntimeException("User or contact not found");
        }
        
        user.removeContact(contactToRemove);
        userRepository.save(user);
        userRepository.save(contactToRemove);
    }
    
    // Get user's contacts
    public Set<User> getUserContacts(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return user.getContacts();
    }
    
    // Search users by name or username
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByNameContainingIgnoreCase(searchTerm);
    }
    
    // Search users by phone number
    public List<User> searchUsersByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumberContaining(phoneNumber);
    }
    
    // Get online contacts
    public Set<User> getOnlineContacts(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        Set<User> contacts = user.getContacts();
        contacts.removeIf(contact -> !contact.isOnline());
        
        return contacts;
    }
    
    // Check if two users are contacts
    public boolean areContacts(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElse(null);
        User user2 = userRepository.findById(userId2).orElse(null);
        
        if (user1 == null || user2 == null) {
            return false;
        }
        
        return user1.getContacts().contains(user2);
    }
}
