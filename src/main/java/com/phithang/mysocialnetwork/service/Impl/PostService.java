package com.phithang.mysocialnetwork.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phithang.mysocialnetwork.dto.CommentDto;
import com.phithang.mysocialnetwork.dto.MediaDto;
import com.phithang.mysocialnetwork.dto.request.PostRequest;
import com.phithang.mysocialnetwork.dto.request.PostUpdateRequest;
import com.phithang.mysocialnetwork.entity.*;
import com.phithang.mysocialnetwork.repository.*;
import com.phithang.mysocialnetwork.service.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService implements IPostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService; // Thêm dependency này

    @Autowired
    private Cloudinary cloudinary;

    @Transactional
    @Override
    public PostEntity createPost(PostRequest postRequest) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity author = userService.findUserByEmail(authentication.getName());

        // Tạo bài viết
        PostEntity postEntity = new PostEntity();
        postEntity.setContent(postRequest.getContent());
        postEntity.setAuthor(author);
        postEntity.setTimestamp(LocalDateTime.now());
        postRepository.save(postEntity);

        List<MediaEntity> mediaEntities = new ArrayList<>();
        for (MediaDto file : postRequest.getMedia()) {
            var uploadResult = cloudinary.uploader().upload(file.getUrl(), ObjectUtils.emptyMap());
            String mediaUrl = uploadResult.get("secure_url").toString();
            String mediaType = file.getType().startsWith("image") ? "IMAGE" : "VIDEO";
            MediaEntity mediaEntity = new MediaEntity();
            mediaEntity.setUrl(mediaUrl);
            mediaEntity.setType(mediaType);
            mediaEntities.add(mediaEntity);
        }

        // Lưu media
        for (MediaEntity media : mediaEntities) {
            media = mediaRepository.save(media);
            PostMediaEntity postMedia = new PostMediaEntity();
            postMedia.setPost(postEntity);
            postMedia.setMedia(media);
            postMediaRepository.save(postMedia);
        }

        return postEntity;
    }

    @Transactional
    @Override
    public PostEntity updatePost(PostUpdateRequest postRequestDto) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity author = userService.findUserByEmail(authentication.getName());

        PostEntity postEntity = postRepository.findById(postRequestDto.getId()).orElse(null);
        if (postEntity == null) return null;

        postEntity.setContent(postRequestDto.getContent());
        postRepository.save(postEntity);

        return postEntity;
    }
    @Transactional
    @Override
    public boolean deletePost(Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        PostEntity postEntity = postRepository.findById(id).orElse(null);
        if (postEntity != null && postEntity.getAuthor().getId().equals(userEntity.getId())) {
            commentRepository.deleteByPostId(id);
            notificationRepository.deleteByPostId(id);
            postRepository.delete(postEntity);

            return true;
        }
        return false;
    }

    @Override
    public boolean likePost(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        PostEntity postEntity = postRepository.findById(id).orElse(null);

        if (postEntity != null && userEntity != null) {
            if (!postEntity.getLikedBy().contains(userEntity)) {
                postEntity.getLikedBy().add(userEntity);
                if (!email.equals(postEntity.getAuthor().getEmail())) {
                    notificationService.createAndSendNotification(
                            postEntity.getAuthor(),
                            userEntity.getFirstname() + " " + userEntity.getLastname() + " đã thích bài viết của bạn.",
                            postEntity
                    );
                }
                postRepository.save(postEntity);
            } else {
                postEntity.getLikedBy().remove(userEntity);
                postRepository.save(postEntity);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean commentPost(Long id, CommentDto commentDto) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.findUserByEmail(email);
            PostEntity postEntity = postRepository.findById(id).orElse(null);

            if (postEntity == null || userEntity == null) return false;

            CommentEntity commentEntity = new CommentEntity();
            commentEntity.setPost(postEntity);
            commentEntity.setAuthor(userEntity);
            commentEntity.setTimestamp(LocalDateTime.now());
            commentEntity.setContent(commentDto.getContent());

            if (commentDto.getParentCommentId() != null) {
                CommentEntity parentComment = commentRepository.findById(commentDto.getParentCommentId()).orElse(null);
                if (parentComment != null) commentEntity.setParentComment(parentComment);
            }

            postEntity.getComments().add(commentEntity);
            postRepository.save(postEntity);

            if (!email.equals(postEntity.getAuthor().getEmail())) {
                notificationService.createAndSendNotification(
                        postEntity.getAuthor(),
                        userEntity.getFirstname() + " " + userEntity.getLastname() + " đã bình luận bài viết của bạn.",
                        postEntity
                );
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error in commentPost: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<PostEntity> getAllPost() {
        return postRepository.findAll();
    }

    @Override
    public List<PostEntity> getMyPost() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        return postRepository.findAllByAuthor(userEntity);
    }

    @Override
    public List<PostEntity> getUserPosts(Long userId) {
        UserEntity userEntity = userService.findById(userId);
        return postRepository.findAllByAuthor(userEntity);
    }

    @Override
    public PostEntity findById(Long id) {
        return postRepository.findById(id).orElse(null);
    }
}