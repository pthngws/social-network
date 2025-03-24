package com.phithang.mysocialnetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.dto.request.PostRequest;
import com.phithang.mysocialnetwork.dto.request.PostUpdateRequest;
import com.phithang.mysocialnetwork.dto.request.ReportRequest;
import com.phithang.mysocialnetwork.dto.response.CommentResponse;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.CommentEntity;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.service.ICommentService;
import com.phithang.mysocialnetwork.service.IPostService;
import com.phithang.mysocialnetwork.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
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

    @PostMapping(value = "/post", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<PostEntity>> post(
            @RequestPart("content") String content,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles) throws IOException {
        PostRequest postRequest = new PostRequest();
        postRequest.setContent(content);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            List<MediaDto> mediaDtos = new ArrayList<>();
            for (MultipartFile file : mediaFiles) {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse<>(400, null, "File media rỗng"));
                }
                MediaDto mediaDto = new MediaDto();
                mediaDto.setUrl(file.getBytes()); // Chuyển file thành byte[]
                mediaDto.setType(file.getContentType());
                mediaDtos.add(mediaDto);
            }
            postRequest.setMedia(mediaDtos);
        }

        try {
            PostEntity postEntity = postService.createPost(postRequest);
            return ResponseEntity.ok(new ApiResponse<>(200, postEntity, "Post created successfully!"));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, null, "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/post/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<PostEntity>> update(
            @PathVariable Long id,
            @RequestPart("content") String content,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "mediaToDelete", required = false) String mediaToDeleteJson) throws IOException {
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest();
        postUpdateRequest.setId(id);
        postUpdateRequest.setContent(content);

        // Xử lý danh sách media cần xóa
        List<String> mediaToDelete = new ArrayList<>();
        if (mediaToDeleteJson != null && !mediaToDeleteJson.isEmpty()) {
            mediaToDelete = Arrays.asList(new ObjectMapper().readValue(mediaToDeleteJson, String[].class));
        }
        postUpdateRequest.setMediaToDelete(mediaToDelete);

        // Xử lý media mới
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            List<MediaDto> mediaDtos = new ArrayList<>();
            for (MultipartFile file : mediaFiles) {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse<>(400, null, "File media rỗng"));
                }
                MediaDto mediaDto = new MediaDto();
                mediaDto.setUrl(file.getBytes());
                mediaDto.setType(file.getContentType());
                mediaDtos.add(mediaDto);
            }
            postUpdateRequest.setMedia(mediaDtos);
        }

        try {
            PostEntity postEntity = postService.updatePost(postUpdateRequest);
            return ResponseEntity.ok(new ApiResponse<>(200, postEntity, "Post updated successfully!"));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, null, "Lỗi hệ thống: " + e.getMessage()));
        }
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
