package com.phithang.mysocialnetwork.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private String authorName;
    private Long authorId;
    private String imageUrl;
    private LocalDateTime timestamp;
    private String replyAuthorName;
    private Long replyId;
    private Long replyAuthorId;
}
