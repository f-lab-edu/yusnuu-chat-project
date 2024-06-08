package com.example.websocektpratice.controller;


import com.example.websocektpratice.Service.ChatService;
import com.example.websocektpratice.model.ChatMessage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Controller
public class ChatController {
    private final SimpMessagingTemplate template;
    private final ChatService chatService;
    private final static String QUEUE_NAME = "hello";

    public ChatController(SimpMessagingTemplate template, ChatService chatService) {
        this.template = template;
        this.chatService = chatService;
    }


    public void sendMassageToClient(ChatMessage recvChatMessage) throws Exception {
        String queueName = recvChatMessage.getRoomId();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ 서버의 주소를 설정합니다. 필요에 따라 변경하세요.
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");

            // Create a new ChatMessage object and set the message
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage(message);

            // Handle the message and send it to the topic
            chatService.handleMessage(chatMessage);
            template.convertAndSend("/topic/public", chatMessage);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public void sendMessageToQueue(ChatMessage chatMessage) throws Exception {
        String queueName = chatMessage.getRoomId();
        System.out.println("Received message: " + chatMessage.getMessage()); // 콘솔 로그 추가
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ 서버의 주소를 설정합니다. 필요에 따라 변경하세요.

        //큐네임을 chatMessage.getRoomId() 로
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            String message = chatMessage.getMessage();
            channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }

    // 지금 고민 두가지
    // 코드 정리하기
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) throws Exception {
        sendMessageToQueue(chatMessage);
        sendMassageToClient(chatMessage);

//        System.out.println("Received message: " + chatMessage.getMessage()); // 콘솔 로그 추가
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost"); // RabbitMQ 서버의 주소를 설정합니다. 필요에 따라 변경하세요.
//
//        //큐네임을 chatMessage.getRoomId() 로
//        try (Connection connection = factory.newConnection();
//             Channel channel = connection.createChannel()) {
//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            String message = chatMessage.getMessage();
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
//            System.out.println(" [x] Sent '" + message + "'");
//            sendMassageToClient(channel);
//        }

    }
}