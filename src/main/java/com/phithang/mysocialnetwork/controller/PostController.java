package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.dto.ResponseDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.service.IPostService;
import com.phithang.mysocialnetwork.service.Impl.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PostController {
    @Autowired
    private IPostService postService;

    @PostMapping("/post")
    public ResponseDto<PostEntity> post(@RequestBody PostDto post) {
        PostEntity postEntity = postService.createPost(post);
        if (postEntity != null) {
            return new ResponseDto<>(200, postEntity, "Success");
        }
        return new ResponseDto<>(400,null,"Fail");
    }
}
