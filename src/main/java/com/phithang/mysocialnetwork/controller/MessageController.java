package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.dto.response.ConversationDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @GetMapping("/getFriendList/{id}")
    public ResponseEntity<List<ConversationDto>> getFriendList(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.findDistinctParticipantsByUserId(id));
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public MessageDto sendMessage(MessageDto chatEntity) {
        messageService.save(chatEntity);
        return chatEntity;
    }

    @GetMapping("/getMessages")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@RequestParam Long senderId, @RequestParam Long receiverId) {
        return ResponseEntity.ok(messageService.findMessagesBetweenUsers(senderId, receiverId));
    }
    @PutMapping("/markMessagesAsRead/{senderId}")
    public ApiResponse<Void> markMessagesAsRead(@PathVariable Long senderId) {
        messageService.markMessagesAsRead(senderId);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Messages marked as read successfully")
                .build();
    }

}