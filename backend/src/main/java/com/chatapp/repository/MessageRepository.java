package com.chatapp.repository;

import com.chatapp.model.ChatRoom;
import com.chatapp.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);
    
    Page<Message> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.chatRoom = :chatRoom ORDER BY m.timestamp DESC")
    List<Message> findLatestMessagesByChatRoom(@Param("chatRoom") ChatRoom chatRoom, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoom = :chatRoom")
    long countByChatRoom(@Param("chatRoom") ChatRoom chatRoom);
}
