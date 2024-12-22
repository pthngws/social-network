package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.dto.PostUpdateDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

public interface IPostService {
    @Transactional
    PostEntity createPost(PostDto postRequestDto) throws IOException;
    @Transactional
    PostEntity updatePost(PostUpdateDto postRequestDto) throws IOException;

    boolean deletePost(Long id);

    boolean likePost(Long id);
}
