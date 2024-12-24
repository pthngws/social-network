package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.PostMediaEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import lombok.Data;

import java.util.List;

@Data
public class PostDto {
    private Long id;
    private String content;
    private String authorName;
    private String imageUrl;// Thông tin tác giả không nhạy cảm
    private int commentCount;
    private int likedByCount;
    private boolean liked;
    private List<PostMediaEntity> media;
    private java.time.LocalDateTime timestamp;

    public PostDto toPostDto(PostEntity postEntity)
    {
        PostDto postDto = new PostDto();
        postDto.setId(postEntity.getId());
        postDto.setContent(postEntity.getContent());
        postDto.setAuthorName(postEntity.getAuthor().getFirstname() + " " + postEntity.getAuthor().getLastname());
        postDto.setImageUrl(postEntity.getAuthor().getImageUrl());
        postDto.setTimestamp(postEntity.getTimestamp());
        postDto.setCommentCount(postEntity.getComments().size());
        postDto.setLikedByCount(postEntity.getLikedBy().size());
        postDto.setMedia(postEntity.getPostMedia());
        return postDto;
    }

}