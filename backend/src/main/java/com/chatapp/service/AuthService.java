package com.chatapp.service;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User signup(String username, String name, String phoneNumber, String password) {
        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("Phone number already registered");
        }
        
        // Create new user
        User user = new User(username, name, phoneNumber, password);
        return userRepository.save(user);
    }
    
    public User login(String usernameOrPhone, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrPhoneNumber(usernameOrPhone);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        // Simple password check (in production, use proper password hashing)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        // Set user online
        user.setOnline(true);
        userRepository.save(user);
        
        return user;
    }
    
    public void logout(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(false);
            userRepository.save(user);
        }
    }
    
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    public boolean isPhoneNumberAvailable(String phoneNumber) {
        return !userRepository.existsByPhoneNumber(phoneNumber);
    }
}
