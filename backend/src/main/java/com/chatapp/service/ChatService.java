package com.chatapp.service;

import com.chatapp.model.ChatRoom;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create or get private chat between two users
    public ChatRoom createOrGetPrivateChat(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElse(null);
        User user2 = userRepository.findById(user2Id).orElse(null);
        
        if (user1 == null || user2 == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if private chat already exists
        Optional<ChatRoom> existingChat = chatRoomRepository.findPrivateChatBetweenUsers(user1, user2);
        
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        
        // Create new private chat
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(user2.getName() + " & " + user1.getName());
        chatRoom.setType(ChatRoom.ChatType.PRIVATE);
        chatRoom.setCreatedBy(user1);
        
        // Add both users as participants
        chatRoom.addParticipant(user1);
        chatRoom.addParticipant(user2);
        
        return chatRoomRepository.save(chatRoom);
    }
    
    // Create group chat
    public ChatRoom createGroupChat(String name, String description, Long createdByUserId, List<Long> participantIds) {
        User createdBy = userRepository.findById(createdByUserId).orElse(null);
        if (createdBy == null) {
            throw new RuntimeException("Creator user not found");
        }
        
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);
        chatRoom.setDescription(description);
        chatRoom.setType(ChatRoom.ChatType.GROUP);
        chatRoom.setCreatedBy(createdBy);
        
        // Add creator as participant
        chatRoom.addParticipant(createdBy);
        
        // Add other participants
        for (Long participantId : participantIds) {
            User participant = userRepository.findById(participantId).orElse(null);
            if (participant != null) {
                chatRoom.addParticipant(participant);
            }
        }
        
        return chatRoomRepository.save(chatRoom);
    }
    
    // Get user's chat rooms
    public List<ChatRoom> getUserChatRooms(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return chatRoomRepository.findByParticipant(user);
    }
    
    // Get private chats for a user
    public List<ChatRoom> getUserPrivateChats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return chatRoomRepository.findPrivateChatsByParticipant(user);
    }
    
    // Get group chats for a user
    public List<ChatRoom> getUserGroupChats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return chatRoomRepository.findGroupChatsByParticipant(user);
    }
    
    // Send message to chat room
    public Message sendMessage(Long chatRoomId, Long senderId, String content, String messageType) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        User sender = userRepository.findById(senderId).orElse(null);
        
        if (chatRoom == null || sender == null) {
            throw new RuntimeException("Chat room or sender not found");
        }
        
        // Check if sender is a participant
        if (!chatRoom.hasParticipant(sender)) {
            throw new RuntimeException("User is not a participant in this chat");
        }
        
        Message message = new Message(sender, chatRoom, content, messageType);
        return messageRepository.save(message);
    }
    
    // Get messages from chat room
    public List<Message> getChatMessages(Long chatRoomId, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            throw new RuntimeException("Chat room not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByChatRoomOrderByTimestampDesc(chatRoom, pageable);
        
        // Reverse to get chronological order
        List<Message> messages = messagePage.getContent();
        java.util.Collections.reverse(messages);
        
        return messages;
    }
    
    // Get latest messages from chat room
    public List<Message> getLatestMessages(Long chatRoomId, int limit) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            throw new RuntimeException("Chat room not found");
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findLatestMessagesByChatRoom(chatRoom, pageable);
    }
    
    // Add participant to group chat
    public ChatRoom addParticipantToGroupChat(Long chatRoomId, Long userId, Long addedByUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        User addedBy = userRepository.findById(addedByUserId).orElse(null);
        
        if (chatRoom == null || user == null || addedBy == null) {
            throw new RuntimeException("Chat room or user not found");
        }
        
        if (chatRoom.getType() != ChatRoom.ChatType.GROUP) {
            throw new RuntimeException("Can only add participants to group chats");
        }
        
        // Check if addedBy is a participant (has permission to add others)
        if (!chatRoom.hasParticipant(addedBy)) {
            throw new RuntimeException("User doesn't have permission to add participants");
        }
        
        chatRoom.addParticipant(user);
        return chatRoomRepository.save(chatRoom);
    }
}
