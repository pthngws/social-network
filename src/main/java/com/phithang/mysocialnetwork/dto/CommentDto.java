package com.phithang.mysocialnetwork.dto;

import lombok.Data;

@Data
public class CommentDto {
    private String content;
    private Long parentCommentId;

}
