package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.CommentDto;
import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.dto.request.PostRequest;
import com.phithang.mysocialnetwork.dto.request.PostUpdateRequest;
import com.phithang.mysocialnetwork.entity.PostEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IPostService {

    @Transactional
    PostEntity createPost(String content, List<MultipartFile> mediaFiles) throws IOException;

    @Transactional
    PostEntity updatePost(Long postId, String content, List<MultipartFile> mediaFiles, String mediaToDeleteJson) throws IOException;

    boolean deletePost(Long id);

    boolean likePost(Long id);

    boolean commentPost(Long id, CommentDto commentDto);

    List<PostDto> getMyPostDtos();

    List<PostDto> getAllPostDtos();

    List<PostDto> getUserPostsDto(Long userId);

    PostEntity findById(Long id);
}
