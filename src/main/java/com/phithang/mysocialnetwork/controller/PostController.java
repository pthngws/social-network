package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.dto.PostUpdateDto;
import com.phithang.mysocialnetwork.dto.ResponseDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.service.IPostService;
import com.phithang.mysocialnetwork.service.Impl.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
public class PostController {
    @Autowired
    private IPostService postService;

    @PostMapping("/post")
    public ResponseDto<PostEntity> post(@RequestBody PostDto post) throws IOException {
        PostEntity postEntity = postService.createPost(post);
        if (postEntity != null) {
            return new ResponseDto<>(200, postEntity, "Success");
        }
        return new ResponseDto<>(400,null,"Fail");
    }

    @PostMapping("/post/update")
    public ResponseDto<PostEntity> update(@RequestBody PostUpdateDto post) throws IOException {
        PostEntity postEntity = postService.updatePost(post);
        if (postEntity != null) {
            return new ResponseDto<>(200, postEntity, "Success");
        }
        return new ResponseDto<>(400,null,"Fail");
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        if (postService.deletePost(id)) {
            return new ResponseEntity<>("Success", HttpStatus.OK);
        }
        return new ResponseEntity<>("Fail", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<String> like(@PathVariable Long id)
    {
        if(postService.likePost(id))
        {
            return ResponseEntity.ok("Like successful!");
        }
        return ResponseEntity.badRequest().body("Like failed!");
    }


}
