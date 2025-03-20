package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IMessageService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MessageController {

    @Autowired
    private IMessageService chatService;

    @GetMapping("/getFriendList/{id}")
    public ResponseEntity<List<Map<String, Object>>> getCustomerList(@PathVariable Long id) {

        List<Map<String, Object>> friends = chatService.findDistinctParticipantsByUserId(id); // receiverID = 1 (cố định hoặc từ token)
        return ResponseEntity.ok(friends);
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public MessageDto sendMessage(MessageDto chatEntity) {
        System.out.println("SenderId: " + chatEntity.getSenderId());
        System.out.println("ReceiverId: " + chatEntity.getReceiverId());
        System.out.println("Content: " + chatEntity.getContent());
        chatEntity.setTimestamp(LocalDateTime.now());
        chatService.save(chatEntity);
        return chatEntity;
    }


    @GetMapping("/getMessages")
    @ResponseBody
    public List<Map<String, Object>> loadMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {
        return chatService.findAll().stream()
                .filter(row -> (row.getSender().getId().equals(senderId) && row.getReceiver().getId().equals(receiverId)) ||
                        (row.getSender().getId().equals(receiverId) && row.getReceiver().getId().equals(senderId)))
                .map(row -> {
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("id", row.getId());
                    messageMap.put("contentMessage", row.getContent());
                    messageMap.put("timestamp", row.getTimestamp());
                    messageMap.put("senderID", row.getSender().getId());
                    messageMap.put("receiverID", row.getReceiver().getId());
                    return messageMap;
                })
                .collect(Collectors.toList());
    }
}
