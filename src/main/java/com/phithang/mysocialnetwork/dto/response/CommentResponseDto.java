package com.phithang.mysocialnetwork.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private String content;
    private String authorName;
    private String imageUrl;
    private LocalDateTime timestamp;
}
