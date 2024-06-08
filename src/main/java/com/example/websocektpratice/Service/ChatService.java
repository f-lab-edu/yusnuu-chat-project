package com.example.websocektpratice.Service;

import com.example.websocektpratice.model.ChatMessage;
import com.example.websocektpratice.model.ChatRoom;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter
public class ChatService {
    private Map<String, ChatRoom> chatRooms = new HashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void handleMessage(ChatMessage chatMessage){
        String roomId = chatMessage.getRoomId();
        ChatRoom chatRoom = chatRooms.get(roomId);
        if (chatRoom == null) {
            chatRoom = new ChatRoom();
            chatRoom.setId(roomId);
            chatRooms.put(roomId, chatRoom);
        }
        chatRoom.getUsers().add(chatMessage.getSender());
        for (String user : chatRoom.getUsers()) {
            messagingTemplate.convertAndSend("/topic/public", user + ": " + chatMessage.getMessage());
        }
    }
}