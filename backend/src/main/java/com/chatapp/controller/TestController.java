package com.chatapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
public class TestController {
    
    @GetMapping("/")
    public String home() {
        return "Chat App Backend is running!";
    }
    
    @GetMapping("/test")
    public String test() {
        return "Backend test endpoint working!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "Backend is healthy!";
    }
}


