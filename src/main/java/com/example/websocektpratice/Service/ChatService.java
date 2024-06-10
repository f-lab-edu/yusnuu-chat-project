package com.example.websocektpratice.Service;

import com.example.websocektpratice.model.ChatMessage;
import com.example.websocektpratice.model.ChatRoom;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Getter
public class ChatService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConnectionFactory rbmqConnectionFactory;
    public ChatService(SimpMessagingTemplate simpMessagingTemplate, ConnectionFactory rbmqConnectionFactory) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.rbmqConnectionFactory = rbmqConnectionFactory;
    }

    public void sendMessageFromRBMQToClient(String roomId) throws Exception {
        String queueName = roomId;
        Connection connection = rbmqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(roomId, false, false, false, null);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageJson = new String(delivery.getBody(), "UTF-8");
            ObjectMapper objectMapper = new ObjectMapper();
            ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
            simpMessagingTemplate.convertAndSend("/topic/public", chatMessage);
        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
    }

    public void sendMessageFromClientToRBMQ(ChatMessage chatMessage) throws Exception {
        String queueName = chatMessage.getRoomId();
        try (Connection connection = rbmqConnectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            ObjectMapper objectMapper = new ObjectMapper();
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            byte[] messageBytes = messageJson.getBytes("UTF-8");
            channel.basicPublish("", queueName, null, messageBytes);
        }
    }
}