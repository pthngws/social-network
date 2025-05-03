package com.phithang.mysocialnetwork.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoryResponseDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String avatar;
    private String content;
    private String musicUrl;
    private LocalDateTime postedAt;
    private LocalDateTime expiresAt;
}
