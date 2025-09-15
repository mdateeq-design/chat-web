package com.chatapp.repository;

import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByUsernameOrPhoneNumber(@Param("identifier") String identifier);
    
    @Query("SELECT u FROM User u WHERE u.isOnline = true")
    List<User> findOnlineUsers();
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% OR u.username LIKE %:name%")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT u FROM User u WHERE u.phoneNumber LIKE %:phone%")
    List<User> findByPhoneNumberContaining(@Param("phone") String phone);
    
    boolean existsByUsername(String username);
    
    boolean existsByPhoneNumber(String phoneNumber);
}
