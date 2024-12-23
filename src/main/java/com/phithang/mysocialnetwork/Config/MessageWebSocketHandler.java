package com.phithang.mysocialnetwork.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private IMessageService messageService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Chuyển tin nhắn nhận được thành đối tượng MessageEntity
        ObjectMapper objectMapper = new ObjectMapper();
        MessageEntity messageEntity = objectMapper.readValue(message.getPayload(), MessageEntity.class);

        // Lưu tin nhắn vào cơ sở dữ liệu
        messageService.saveMessage(messageEntity);

        // Gửi tin nhắn trả lại cho người nhận qua WebSocket
        TextMessage reply = new TextMessage("Message from " + messageEntity.getSender().getId() + ": " + messageEntity.getContent());
        session.sendMessage(reply);
    }
}