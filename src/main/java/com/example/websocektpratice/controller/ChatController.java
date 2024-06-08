package com.example.websocektpratice.controller;


import com.example.websocektpratice.Service.ChatService;
import com.example.websocektpratice.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ChatController {
    private SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final static String QUEUE_NAME = "hello";


    public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) throws Exception {
        chatService.sendMessageFromClientToRBMQ(chatMessage);
        chatService.sendMassageFromRBMQToClient(chatMessage);

    }
}