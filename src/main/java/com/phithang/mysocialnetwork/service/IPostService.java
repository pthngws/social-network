package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.CommentDto;
import com.phithang.mysocialnetwork.dto.request.PostRequest;
import com.phithang.mysocialnetwork.dto.request.PostUpdateRequest;
import com.phithang.mysocialnetwork.entity.PostEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

public interface IPostService {
    @Transactional
    PostEntity createPost(PostRequest postRequest) throws IOException;
    @Transactional
    PostEntity updatePost(PostUpdateRequest postRequestDto) throws IOException;

    boolean deletePost(Long id);

    boolean likePost(Long id);

    boolean commentPost(Long id, CommentDto commentDto);

    List<PostEntity> getAllPost();

    List<PostEntity> getMyPost();

    List<PostEntity> getUserPosts(Long userId);

    PostEntity findById(Long id);
}
