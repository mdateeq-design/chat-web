package com.chatapp.repository;

import com.chatapp.model.ChatRoom;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p = :user")
    List<ChatRoom> findByParticipant(@Param("user") User user);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = 'PRIVATE' AND :user1 MEMBER OF cr.participants AND :user2 MEMBER OF cr.participants")
    Optional<ChatRoom> findPrivateChatBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = 'GROUP' AND :user MEMBER OF cr.participants")
    List<ChatRoom> findGroupChatsByParticipant(@Param("user") User user);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = 'PRIVATE' AND :user MEMBER OF cr.participants")
    List<ChatRoom> findPrivateChatsByParticipant(@Param("user") User user);
}
