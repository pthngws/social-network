package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import org.springframework.transaction.annotation.Transactional;

public interface IPostService {
    @Transactional
    PostEntity createPost(PostDto postRequestDto);
}
