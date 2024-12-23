package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageEntity messageEntity) {
        // Gửi tin nhắn qua WebSocket sẽ được xử lý trong WebSocketHandler
        messageService.saveMessage(messageEntity);
        return "Message sent successfully";
    }
}
