package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.PostMediaEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class PostDto {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorImageUrl;
    private LocalDateTime timestamp;
    private List<String> mediaUrls;
    private int commentCount;
    private String reactionType; // Biểu cảm của người dùng hiện tại
    private Map<String, Long> reactionCounts; // Số lượng từng loại biểu cảm

    public PostDto toPostDto(PostEntity postEntity) {
        PostDto postDto = new PostDto();
        postDto.setId(postEntity.getId());
        postDto.setContent(postEntity.getContent());
        postDto.setAuthorId(postEntity.getAuthor().getId());
        postDto.setAuthorName(postEntity.getAuthor().getFirstname() + " " + postEntity.getAuthor().getLastname());
        postDto.setAuthorImageUrl(postEntity.getAuthor().getImageUrl());
        postDto.setTimestamp(postEntity.getTimestamp());
        postDto.setCommentCount(postEntity.getComments() != null ? postEntity.getComments().size() : 0);

        List<String> mediaUrls = postEntity.getPostMedia() != null
                ? postEntity.getPostMedia().stream()
                .map(PostMediaEntity::getMedia)
                .map(media -> media.getUrl())
                .collect(Collectors.toList())
                : new ArrayList<>();
        postDto.setMediaUrls(mediaUrls);

        return postDto;
    }
}