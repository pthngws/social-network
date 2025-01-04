package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.CommentDto;
import com.phithang.mysocialnetwork.dto.request.PostRequestDto;
import com.phithang.mysocialnetwork.dto.request.PostUpdateDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

public interface IPostService {
    @Transactional
    PostEntity createPost(PostRequestDto postRequestDto) throws IOException;
    @Transactional
    PostEntity updatePost(PostUpdateDto postRequestDto) throws IOException;

    boolean deletePost(Long id);

    boolean likePost(Long id);

    boolean commentPost(Long id, CommentDto commentDto);

    List<PostEntity> getAllPost();

    List<PostEntity> getMyPost();

    PostEntity findById(Long id);
}
