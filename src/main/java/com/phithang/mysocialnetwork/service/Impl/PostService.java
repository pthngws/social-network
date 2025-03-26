package com.phithang.mysocialnetwork.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phithang.mysocialnetwork.dto.CommentDto;
import com.phithang.mysocialnetwork.dto.PostDto;
import com.phithang.mysocialnetwork.entity.*;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.*;
import com.phithang.mysocialnetwork.service.IPostService;
import com.phithang.mysocialnetwork.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private ReportRepository reportRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Transactional
    @Override
    public PostEntity createPost(String content, List<MultipartFile> mediaFiles) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        UserEntity author = userService.findUserByEmail(authentication.getName());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        PostEntity postEntity = new PostEntity();
        postEntity.setContent(content);
        postEntity.setAuthor(author);
        postEntity.setTimestamp(LocalDateTime.now());
        postRepository.save(postEntity);

        // Xử lý media
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            if (mediaFiles.size() > 10) {
                throw new AppException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
            }

            List<MediaEntity> mediaEntities = new ArrayList<>();
            for (MultipartFile file : mediaFiles) {
                if (file.isEmpty()) {
                    throw new AppException(ErrorCode.MEDIA_DATA_INVALID);
                }

                String resourceType = file.getContentType().startsWith("image") ? "image" : "video";
                Map uploadParams = ObjectUtils.asMap("resource_type", resourceType);

                // Upload lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                String mediaUrl = uploadResult.get("secure_url").toString();
                String mediaType = file.getContentType().startsWith("image") ? "IMAGE" : "VIDEO";

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
    public PostEntity updatePost(Long postId, String content, List<MultipartFile> mediaFiles, String mediaToDeleteJson) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        UserEntity author = userService.findUserByEmail(authentication.getName());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (!postEntity.getAuthor().equals(author)) {
            throw new AppException(ErrorCode.POST_VISIBILITY_UNAUTHORIZED);
        }

        // Cập nhật nội dung bài viết
        postEntity.setContent(content);

        // Xử lý danh sách media cần xóa
        if (mediaToDeleteJson != null && !mediaToDeleteJson.isEmpty()) {
            List<String> mediaToDelete = Arrays.asList(new ObjectMapper().readValue(mediaToDeleteJson, String[].class));
            if (!mediaToDelete.isEmpty()) {
                List<PostMediaEntity> postMediaEntities = postMediaRepository.findByPostId(postEntity.getId());
                for (PostMediaEntity postMedia : new ArrayList<>(postMediaEntities)) {
                    if (mediaToDelete.contains(postMedia.getMedia().getUrl())) {
                        try {
                            // Lấy publicId từ URL
                            String url = postMedia.getMedia().getUrl();
                            String fileName = url.substring(url.lastIndexOf("/") + 1);
                            String publicId = fileName.substring(0, fileName.lastIndexOf(".")); // Bỏ đuôi file

                            // Xóa trên Cloudinary
                            cloudinary.uploader().destroy(publicId,
                                    ObjectUtils.asMap("resource_type", postMedia.getMedia().getType().equals("IMAGE") ? "image" : "video"));

                            // Xóa liên kết và media
                            postMediaRepository.delete(postMedia);
                            mediaRepository.delete(postMedia.getMedia());
                        } catch (Exception e) {
                            System.err.println("Lỗi khi xóa media trên Cloudinary: " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Thêm media mới nếu có
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            if (mediaFiles.size() > 10) {
                throw new AppException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
            }

            List<MediaEntity> mediaEntities = new ArrayList<>();
            for (MultipartFile file : mediaFiles) {
                if (file.isEmpty()) {
                    throw new AppException(ErrorCode.MEDIA_DATA_INVALID);
                }

                String resourceType = file.getContentType().startsWith("image") ? "image" : "video";
                Map uploadParams = ObjectUtils.asMap("resource_type", resourceType);

                // Upload lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                String mediaUrl = uploadResult.get("secure_url").toString();
                String mediaType = file.getContentType().startsWith("image") ? "IMAGE" : "VIDEO";

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

        return postRepository.save(postEntity);
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
            reportRepository.deleteByPostId(id);
            // 2. Xóa PostMediaEntity trước
            List<PostMediaEntity> postMediaEntities = postMediaRepository.findByPostId(id);
            for (PostMediaEntity postMedia : postMediaEntities) {
                MediaEntity media = postMedia.getMedia();
                if (media != null) {
                    // Xóa trên Cloudinary
                    String publicId = media.getUrl().substring(media.getUrl().lastIndexOf("/") + 1).split("\\.")[0];
                    cloudinary.uploader().destroy(publicId, Map.of("resource_type", media.getType().equals("IMAGE") ? "image" : "video"));

                    // **XÓA LIÊN KẾT POST_MEDIA TRƯỚC**
                    postMediaRepository.delete(postMedia);

                    // **XÓA MEDIAENTITY SAU**
                    mediaRepository.delete(media);
                }
            }

            // 3. Xóa các lượt thích (bản ghi trong bảng post_likes)
            postEntity.getLikedBy().clear(); // Xóa quan hệ ManyToMany
            postRepository.save(postEntity); // Cập nhật bảng post_likes trước khi xóa bài viết

            // 4. Xóa bài viết
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
        commentEntity.setTimestamp(java.time.LocalDateTime.now());
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
    public List<PostDto> getMyPostDtos() {
        String email = getCurrentUserEmail();
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        return convertToPostDtos(postRepository.findAllByAuthor(user), email);
    }
    @Override
    public List<PostDto> getAllPostDtos() {
            return convertToPostDtos(postRepository.findAll(), getCurrentUserEmail());
        }

    private List<PostDto> convertToPostDtos(List<PostEntity> posts, String currentUserEmail) {
        return posts.stream()
                .map(post -> buildPostDto(post, currentUserEmail))
                .sorted(Comparator.comparing(PostDto::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    @Override
    public List<PostDto> getUserPostsDto(Long userId) {
        UserEntity user = userService.findById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        String currentUserEmail = getCurrentUserEmail();
        return postRepository.findAllByAuthor(user).stream()
                .map(post -> buildPostDto(post, currentUserEmail))
                .sorted(Comparator.comparing(PostDto::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public PostEntity findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }
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
}