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
import java.util.Map;

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
        postRepository.save(postEntity);

        if (postRequest.getMedia() != null && !postRequest.getMedia().isEmpty()) {
            if (postRequest.getMedia().size() > 10) {
                throw new AppException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
            }

            List<MediaEntity> mediaEntities = new ArrayList<>();
            for (MediaDto mediaDto : postRequest.getMedia()) {
                // Kiểm tra dữ liệu file
                if (mediaDto.getUrl() == null || mediaDto.getUrl().length == 0) {
                    throw new AppException(ErrorCode.MEDIA_DATA_INVALID);
                }

                String resourceType = mediaDto.getType().startsWith("image") ? "image" : "video";
                Map uploadParams = ObjectUtils.asMap("resource_type", resourceType);

                // Upload lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(mediaDto.getUrl(), uploadParams);
                String mediaUrl = uploadResult.get("secure_url").toString();
                String mediaType = mediaDto.getType().startsWith("image") ? "IMAGE" : "VIDEO";

                MediaEntity mediaEntity = new MediaEntity();
                mediaEntity.setUrl(mediaUrl);
                mediaEntity.setType(mediaType);
                mediaEntities.add(mediaEntity);
            }

            // Lưu media và liên kết với bài viết
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

        System.out.println("Bắt đầu cập nhật bài viết: " + postEntity.getId());

        // Cập nhật nội dung
        postEntity.setContent(postRequestDto.getContent());

        if (postRequestDto.getMediaToDelete() != null && !postRequestDto.getMediaToDelete().isEmpty()) {
            List<PostMediaEntity> postMediaEntities = postMediaRepository.findByPostId(postEntity.getId());
            for (PostMediaEntity postMedia : new ArrayList<>(postMediaEntities)) {
                if (postRequestDto.getMediaToDelete().contains(postMedia.getMedia().getUrl())) {
                    try {
                        // Lấy publicId từ URL
                        String url = postMedia.getMedia().getUrl();
                        String[] urlParts = url.split("/");
                        String fileName = urlParts[urlParts.length - 1];
                        String publicId = fileName.split("\\.")[0];

                        // Xóa trên Cloudinary
                        cloudinary.uploader().destroy(
                                publicId,
                                ObjectUtils.asMap("resource_type", postMedia.getMedia().getType().equals("IMAGE") ? "image" : "video")
                        );

                        // Xóa liên kết PostMediaEntity
                        postMediaRepository.delete(postMedia);

                        // Xóa MediaEntity
                        mediaRepository.delete(postMedia.getMedia());

                        // Không cần gọi postEntity.getPostMedia().remove(postMedia) vì Hibernate sẽ tự động cập nhật danh sách
                    } catch (Exception e) {
                        System.err.println("Lỗi khi xóa media trên Cloudinary: " + e.getMessage());
                    }
                }
            }
        }
        // Thêm media mới nếu có
        if (postRequestDto.getMedia() != null && !postRequestDto.getMedia().isEmpty()) {
            if (postRequestDto.getMedia().size() > 10) {
                throw new AppException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
            }

            List<MediaEntity> mediaEntities = new ArrayList<>();
            for (MediaDto mediaDto : postRequestDto.getMedia()) {
                if (mediaDto.getUrl() == null || mediaDto.getUrl().length == 0) {
                    throw new AppException(ErrorCode.MEDIA_DATA_INVALID);
                }

                String resourceType = mediaDto.getType().startsWith("image") ? "image" : "video";
                Map uploadParams = ObjectUtils.asMap("resource_type", resourceType);

                Map uploadResult = cloudinary.uploader().upload(mediaDto.getUrl(), uploadParams);
                String mediaUrl = uploadResult.get("secure_url").toString();
                String mediaType = mediaDto.getType().startsWith("image") ? "IMAGE" : "VIDEO";

                MediaEntity mediaEntity = new MediaEntity();
                mediaEntity.setUrl(mediaUrl);
                mediaEntity.setType(mediaType);
                mediaEntities.add(mediaEntity);
            }

            for (MediaEntity media : mediaEntities) {
                media = mediaRepository.save(media);
                PostMediaEntity postMedia = new PostMediaEntity();
                postMedia.setPost(postEntity);
                postMedia.setMedia(media);
                postMediaRepository.save(postMedia);
            }
        }

        try {
            System.out.println("Lưu bài viết: " + postEntity.getId());
            PostEntity existingPost = postRepository.findById(postEntity.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Bài viết đã bị xóa"));
            PostEntity updatedPost = postRepository.save(postEntity);
            System.out.println("Lưu bài viết thành công: " + updatedPost.getId());
            return updatedPost;
        } catch (Exception e) {
            throw new AppException(ErrorCode.POST_UPDATE_FAILED, "Lỗi khi lưu bài viết: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public boolean deletePost(Long id) {
        // Kiểm tra xác thực người dùng
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Lấy email người dùng hiện tại
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        // Tìm bài viết
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Kiểm tra quyền xóa bài viết
        if (!postEntity.getAuthor().getId().equals(userEntity.getId())) {
            throw new AppException(ErrorCode.POST_VISIBILITY_UNAUTHORIZED);
        }

        try {
            // 1. Xóa các thông báo liên quan đến bài viết
            notificationRepository.deleteByPostId(id);

            commentRepository.deleteByPostId(id);

            List<PostMediaEntity> postMediaEntities = postMediaRepository.findByPostId(id);
            for (PostMediaEntity postMedia : postMediaEntities) {
                MediaEntity media = postMedia.getMedia();
                if (media != null) {
                    // Delete from Cloudinary
                    String publicId = media.getUrl().substring(media.getUrl().lastIndexOf("/") + 1).split("\\.")[0];
                    cloudinary.uploader().destroy(publicId, Map.of("resource_type", media.getType().equals("IMAGE") ? "image" : "video"));

                    // Set media reference to null and save
                    postMedia.setMedia(null);
                    postMediaRepository.save(postMedia);

                    // Delete entities
                    mediaRepository.delete(media);
                    postMediaRepository.delete(postMedia);
                }
            }

            // 4. Xóa các lượt thích (bản ghi trong bảng post_likes)
            postEntity.getLikedBy().clear(); // Xóa quan hệ ManyToMany
            postRepository.save(postEntity); // Cần lưu để cập nhật bảng post_likes


            // 5. Xóa bài viết
            postRepository.delete(postEntity);


            return true;
        } catch (Exception e) {

            throw new AppException(ErrorCode.POST_DELETE_FAILED, "Failed to delete post: " + e.getMessage());
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