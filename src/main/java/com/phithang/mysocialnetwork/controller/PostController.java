package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.dto.request.PostRequestDto;
import com.phithang.mysocialnetwork.dto.request.PostUpdateDto;
import com.phithang.mysocialnetwork.dto.response.ResponseDto;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.service.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping
public class PostController {
    @Autowired
    private IPostService postService;

    @GetMapping("/myposts")
    public ResponseDto<Iterable<PostDto>> getMyPosts() {
        // Lấy tất cả bài viết
        List<PostEntity> posts = postService.getMyPost();
        List<PostDto> list = new ArrayList<>();

        // Lấy người dùng hiện tại từ SecurityContextHolder
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        for (PostEntity postEntity : posts) {
            PostDto postDto = new PostDto();

            // Kiểm tra xem bài viết có ai đã like không, tránh lỗi khi likedBy rỗng
            boolean isLiked = false;
            if (!postEntity.getLikedBy().isEmpty()) {
                // Kiểm tra xem người dùng hiện tại có trong danh sách thích không
                isLiked = postEntity.getLikedBy().get(0).getEmail().equals(currentUserEmail);
            }

            // Cập nhật trường liked trong PostDto
            postDto.setLiked(isLiked);

            // Chuyển đổi PostEntity sang PostDto
            list.add(postDto.toPostDto(postEntity));
        }
        list.reversed();

        // Trả về ResponseDto với trạng thái thành công và danh sách bài viết
        return new ResponseDto<>(200, list, "Get posts successful!");
    }
    @GetMapping("/posts")
    public ResponseDto<Iterable<PostDto>> getPosts() {
        // Lấy tất cả bài viết
        List<PostEntity> posts = postService.getAllPost();
        List<PostDto> list = new ArrayList<>();

        // Lấy người dùng hiện tại từ SecurityContextHolder
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        for (PostEntity postEntity : posts) {
            PostDto postDto = new PostDto();

            // Kiểm tra xem bài viết có ai đã like không, tránh lỗi khi likedBy rỗng
            boolean isLiked = false;
            if (!postEntity.getLikedBy().isEmpty()) {
                // Kiểm tra xem người dùng hiện tại có trong danh sách thích không
                isLiked = postEntity.getLikedBy().get(0).getEmail().equals(currentUserEmail);
            }

            // Cập nhật trường liked trong PostDto
            postDto.setLiked(isLiked);

            // Chuyển đổi PostEntity sang PostDto
            list.add(postDto.toPostDto(postEntity));
        }
        list.reversed();
        // Trả về ResponseDto với trạng thái thành công và danh sách bài viết
        return new ResponseDto<>(200, list, "Get posts successful!");
    }


    @PostMapping("/post")
    public ResponseDto<PostEntity> post(@RequestBody PostRequestDto post) throws IOException {
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

    @PostMapping("/comment/{id}")
    public ResponseEntity<String> comment(@PathVariable Long id, @RequestBody CommentDto commentDto)
        {
        if(postService.commentPost(id,commentDto))
        {
            return ResponseEntity.ok("Comment successful!");
        }
        return ResponseEntity.badRequest().body("Comment failed!");
        }




}
