package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.dto.request.PostRequest;
import com.phithang.mysocialnetwork.dto.request.PostUpdateRequest;
import com.phithang.mysocialnetwork.dto.request.ReportRequest;
import com.phithang.mysocialnetwork.dto.response.CommentResponse;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.CommentEntity;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.service.ICommentService;
import com.phithang.mysocialnetwork.service.IPostService;
import com.phithang.mysocialnetwork.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class PostController {

    @Autowired
    private IPostService postService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IReportService reportService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private PostDto buildPostDto(PostEntity postEntity, String currentUserEmail) {
        PostDto postDto = new PostDto().toPostDto(postEntity);

        boolean isLiked = Optional.ofNullable(postEntity.getLikedBy())
                .orElse(new ArrayList<>())
                .stream()
                .anyMatch(user -> user.getEmail().equals(currentUserEmail));

        postDto.setLiked(isLiked);
        return postDto;
    }

    @GetMapping("/userpost/{id}")
    public ResponseEntity<ApiResponse<List<PostDto>>> getUserPosts(@PathVariable Long id) {
        List<PostEntity> posts = postService.getUserPosts(id);
        String currentUserEmail = getCurrentUserEmail();

        List<PostDto> list = posts.stream()
                .map(post -> buildPostDto(post, currentUserEmail))
                .collect(Collectors.toList());
        Collections.reverse(list);

        return ResponseEntity.ok(new ApiResponse<>(200, list, "Get posts successful!"));
    }

    @GetMapping("/myposts")
    public ResponseEntity<ApiResponse<List<PostDto>>> getMyPosts() {
        List<PostEntity> posts = postService.getMyPost();
        String currentUserEmail = getCurrentUserEmail();

        List<PostDto> list = posts.stream()
                .map(post -> buildPostDto(post, currentUserEmail))
                .collect(Collectors.toList());
        Collections.reverse(list);

        return ResponseEntity.ok(new ApiResponse<>(200, list, "Get my posts successful!"));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostDto>>> getPosts() {
        List<PostEntity> posts = postService.getAllPost();
        String currentUserEmail = getCurrentUserEmail();

        List<PostDto> list = posts.stream()
                .map(post -> buildPostDto(post, currentUserEmail))
                .collect(Collectors.toList());
        Collections.reverse(list);

        return ResponseEntity.ok(new ApiResponse<>(200, list, "Get all posts successful!"));
    }

    @PostMapping("/post")
    public ResponseEntity<ApiResponse<PostEntity>> post(@RequestBody PostRequest post) throws IOException {
        PostEntity postEntity = postService.createPost(post);
        if (postEntity != null) {
            return ResponseEntity.ok(new ApiResponse<>(200, postEntity, "Post created successfully!"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Post creation failed!"));
    }

    @PutMapping("/post/{id}")
    public ResponseEntity<ApiResponse<PostEntity>> update(@RequestBody PostUpdateRequest post) throws IOException {
        PostEntity postEntity = postService.updatePost(post);
        if (postEntity != null) {
            return ResponseEntity.ok(new ApiResponse<>(200, postEntity, "Post updated successfully!"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Post update failed!"));
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (postService.deletePost(id)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Post deleted successfully!"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Post deletion failed!"));
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<ApiResponse<Void>> like(@PathVariable Long id) {
        if (postService.likePost(id)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Like successful!"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Like failed!"));
    }

    @PostMapping("/comment/{postId}")
    public ResponseEntity<ApiResponse<CommentDto>> comment(@PathVariable Long postId, @RequestBody CommentDto commentDto) {
        if (postService.commentPost(postId, commentDto)) {
            return ResponseEntity.ok(new ApiResponse<>(200, commentDto, "Comment successful!"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Comment failed!"));
    }

    @GetMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long id) {
        PostEntity postEntity = postService.findById(id);

        if (postEntity != null) {
            List<CommentEntity> comments = postEntity.getComments();
            List<CommentResponse> list = comments.stream().map(commentEntity -> {
                CommentResponse dto = new CommentResponse();
                dto.setId(commentEntity.getId());
                dto.setAuthorId(commentEntity.getAuthor().getId());
                dto.setContent(commentEntity.getContent());
                dto.setAuthorName(commentEntity.getAuthor().getFirstname() + " " + commentEntity.getAuthor().getLastname());
                dto.setImageUrl(commentEntity.getAuthor().getImageUrl());
                dto.setTimestamp(commentEntity.getTimestamp());

                if (commentEntity.getParentComment() != null) {
                    dto.setReplyAuthorName(commentEntity.getParentComment().getAuthor().getFirstname() + " " + commentEntity.getParentComment().getAuthor().getLastname());
                    dto.setReplyId(commentEntity.getParentComment().getId());
                    dto.setReplyAuthorId(commentEntity.getParentComment().getAuthor().getId());
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(200, list, "Get comments successful!"));
        }

        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Post not found!"));
    }

    @DeleteMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        CommentEntity commentEntity = commentService.findById(id);

        if (commentEntity != null && commentService.delete(commentEntity)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Comment deleted successfully!"));
        }

        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Comment deletion failed!"));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> report(@RequestBody ReportRequest reportRequest) {
        if (reportService.saveReport(reportRequest)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Report successful!"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Report failed!"));
    }
}
