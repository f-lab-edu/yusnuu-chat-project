package com.example.websocektpratice.Service;

import com.example.websocektpratice.model.ChatMessage;
import com.example.websocektpratice.model.ChatRoom;
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

    private Map<String, ChatRoom> chatRooms = new HashMap<>();
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConnectionFactory rbmqConnectionFactory;
    public ChatService(SimpMessagingTemplate simpMessagingTemplate, ConnectionFactory rbmqConnectionFactory) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.rbmqConnectionFactory = rbmqConnectionFactory;
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
            simpMessagingTemplate.convertAndSend("/topic/public", user + ": " + chatMessage.getMessage());
        }
    }


    public void sendMassageFromRBMQToClient(ChatMessage recvChatMessage) throws Exception {
        String queueName = recvChatMessage.getRoomId();
        Connection connection = rbmqConnectionFactory.newConnection();
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
            handleMessage(chatMessage);
            simpMessagingTemplate.convertAndSend("/topic/public", chatMessage);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public void sendMessageFromClientToRBMQ(ChatMessage chatMessage) throws Exception {
        String queueName = chatMessage.getRoomId();
        System.out.println("Received message: " + chatMessage.getMessage()); // 콘솔 로그 추가

        //큐네임을 chatMessage.getRoomId() 로
        try (Connection connection = rbmqConnectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            String message = chatMessage.getMessage();
            channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }


}