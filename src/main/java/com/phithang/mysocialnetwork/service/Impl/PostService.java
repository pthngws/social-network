package com.phithang.mysocialnetwork.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phithang.mysocialnetwork.dto.CommentDto;
import com.phithang.mysocialnetwork.dto.MediaDto;
import com.phithang.mysocialnetwork.dto.request.PostRequest;
import com.phithang.mysocialnetwork.dto.request.PostUpdateRequest;
import com.phithang.mysocialnetwork.entity.*;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
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
    private NotificationService notificationService;

    @Autowired
    private Cloudinary cloudinary;

    @Transactional
    @Override
    public PostEntity createPost(PostRequest postRequest) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        UserEntity author = userService.findUserByEmail(authentication.getName());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        PostEntity postEntity = new PostEntity();
        postEntity.setContent(postRequest.getContent());
        postEntity.setAuthor(author);
        postEntity.setTimestamp(LocalDateTime.now());
        try {
            postRepository.save(postEntity);
        } catch (Exception e) {
            throw new AppException(ErrorCode.POST_CREATION_FAILED);
        }

        List<MediaEntity> mediaEntities = new ArrayList<>();
        if (postRequest.getMedia() != null && !postRequest.getMedia().isEmpty()) {
            if (postRequest.getMedia().size() > 10) { // Giả sử giới hạn là 10 media
                throw new AppException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
            }
            for (MediaDto file : postRequest.getMedia()) {
                try {
                    var uploadResult = cloudinary.uploader().upload(file.getUrl(), ObjectUtils.emptyMap());
                    String mediaUrl = uploadResult.get("secure_url").toString();
                    String mediaType = file.getType().startsWith("image") ? "IMAGE" : "VIDEO";
                    MediaEntity mediaEntity = new MediaEntity();
                    mediaEntity.setUrl(mediaUrl);
                    mediaEntity.setType(mediaType);
                    mediaEntities.add(mediaEntity);
                } catch (Exception e) {
                    throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
                }
            }

            for (MediaEntity media : mediaEntities) {
                media = mediaRepository.save(media);
                PostMediaEntity postMedia = new PostMediaEntity();
                postMedia.setPost(postEntity);
                postMedia.setMedia(media);
                postMediaRepository.save(postMedia);
            }
        }

        return postEntity;
    }

    @Transactional
    @Override
    public PostEntity updatePost(PostUpdateRequest postRequestDto) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        UserEntity author = userService.findUserByEmail(authentication.getName());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        PostEntity postEntity = postRepository.findById(postRequestDto.getId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (!postEntity.getAuthor().equals(author)) {
            throw new AppException(ErrorCode.POST_VISIBILITY_UNAUTHORIZED);
        }

        postEntity.setContent(postRequestDto.getContent());
        try {
            return postRepository.save(postEntity);
        } catch (Exception e) {
            throw new AppException(ErrorCode.POST_UPDATE_FAILED);
        }
    }

    @Transactional
    @Override
    public boolean deletePost(Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (!postEntity.getAuthor().getId().equals(userEntity.getId())) {
            throw new AppException(ErrorCode.POST_VISIBILITY_UNAUTHORIZED);
        }
        try {
            commentRepository.deleteByPostId(id);
            notificationRepository.deleteByPostId(id);
            postRepository.delete(postEntity);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.POST_DELETE_FAILED);
        }
    }

    @Override
    public boolean likePost(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (!postEntity.getLikedBy().contains(userEntity)) {
            postEntity.getLikedBy().add(userEntity);
            if (!email.equals(postEntity.getAuthor().getEmail())) {
                notificationService.createAndSendNotification(
                        postEntity.getAuthor(),
                        userEntity.getFirstname() + " " + userEntity.getLastname() + " đã thích bài viết của bạn.",
                        postEntity
                );
            }
        } else {
            postEntity.getLikedBy().remove(userEntity);
        }
        try {
            postRepository.save(postEntity);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.POST_UPDATE_FAILED);
        }
    }

    @Override
    public boolean commentPost(Long id, CommentDto commentDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setPost(postEntity);
        commentEntity.setAuthor(userEntity);
        commentEntity.setTimestamp(LocalDateTime.now());
        commentEntity.setContent(commentDto.getContent());

        if (commentDto.getParentCommentId() != null) {
            CommentEntity parentComment = commentRepository.findById(commentDto.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
            commentEntity.setParentComment(parentComment);
        }

        postEntity.getComments().add(commentEntity);
        try {
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
            throw new AppException(ErrorCode.COMMENT_CREATION_FAILED);
        }
    }

    @Override
    public List<PostEntity> getAllPost() {
        return postRepository.findAll();
    }

    @Override
    public List<PostEntity> getMyPost() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        return postRepository.findAllByAuthor(userEntity);
    }

    @Override
    public List<PostEntity> getUserPosts(Long userId) {
        UserEntity userEntity = userService.findById(userId);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        return postRepository.findAllByAuthor(userEntity);
    }

    @Override
    public PostEntity findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }
}