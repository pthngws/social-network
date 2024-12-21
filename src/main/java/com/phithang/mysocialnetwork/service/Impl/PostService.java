package com.phithang.mysocialnetwork.service.Impl;


import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.PostRepository;
import com.phithang.mysocialnetwork.service.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PostService implements IPostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MediaService mediaService;

    @Transactional
    @Override
    public PostEntity createPost(PostDto postRequestDto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity author = userService.findUserByEmail(authentication.getName());

        // Tạo bài viết
        PostEntity postEntity = new PostEntity();
        postEntity.setContent(postRequestDto.getContent());
        postEntity.setAuthor(author);
        postEntity.setTimestamp(LocalDateTime.now());
        postRepository.save(postEntity);
//
//        // Lưu media (nếu có)
//        if (postRequestDto.getMedia() != null && !postRequestDto.getMedia().isEmpty()) {
//            for (MediaDto mediaDto : postRequestDto.getMedia()) {
//                MediaEntity mediaEntity = mediaService.createMedia(mediaDto);
//                mediaService.linkMediaToPost(postEntity, mediaEntity);
//            }
//        }
        return postEntity;
    }

}
