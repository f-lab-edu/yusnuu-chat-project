package com.example.websocektpratice.controller;


import com.example.websocektpratice.Service.ChatService;
import com.example.websocektpratice.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chatPageRouter() {
        return "chat";
    }
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) throws Exception {
        chatService.sendMessageFromClientToRBMQ(chatMessage);
        chatService.sendMessageFromRBMQToClient(chatMessage.getRoomId());
    }
}