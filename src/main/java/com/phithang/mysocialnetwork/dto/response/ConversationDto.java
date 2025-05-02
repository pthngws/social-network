package com.phithang.mysocialnetwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private Long userID; // ID của người kia
    private String name; // Tên người kia
    private String avatar; // URL ảnh đại diện
    private String content; // Nội dung tin nhắn cuối cùng
    private Long unreadCount; // Số tin nhắn chưa đọc
    private LocalDateTime lastMessageTime; // Thời gian tin nhắn cuối cùng
    private Boolean isOnline; // Trạng thái online
    private Long minutesAgo; // Thời gian offline (nếu không online)
    private Boolean isSender;
}