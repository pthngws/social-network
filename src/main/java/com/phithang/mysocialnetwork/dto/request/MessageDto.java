package com.phithang.mysocialnetwork.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private String content;
    private Long senderId;
    private Long receiverId;
    private LocalDateTime timestamp;

}
