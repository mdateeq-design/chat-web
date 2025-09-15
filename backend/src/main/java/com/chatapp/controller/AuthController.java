package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            User user = authService.signup(
                request.getUsername(),
                request.getName(),
                request.getPhoneNumber(),
                request.getPassword()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.login(request.getUsernameOrPhone(), request.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/logout/{userId}")
    public ResponseEntity<?> logout(@PathVariable Long userId) {
        authService.logout(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        boolean available = authService.isUsernameAvailable(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-phone/{phoneNumber}")
    public ResponseEntity<?> checkPhone(@PathVariable String phoneNumber) {
        boolean available = authService.isPhoneNumberAvailable(phoneNumber);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("name", user.getName());
        userResponse.put("phoneNumber", user.getPhoneNumber());
        userResponse.put("isOnline", user.isOnline());
        userResponse.put("createdAt", user.getCreatedAt());
        return userResponse;
    }
    
    // Request DTOs
    public static class SignupRequest {
        private String username;
        private String name;
        private String phoneNumber;
        private String password;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class LoginRequest {
        private String usernameOrPhone;
        private String password;
        
        // Getters and setters
        public String getUsernameOrPhone() { return usernameOrPhone; }
        public void setUsernameOrPhone(String usernameOrPhone) { this.usernameOrPhone = usernameOrPhone; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
