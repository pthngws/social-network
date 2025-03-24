package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.entity.CommentEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.CommentRepository;
import com.phithang.mysocialnetwork.service.ICommentService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService implements ICommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IUserService userService;

    @Override
    public CommentEntity findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Override
    @Transactional
    public boolean delete(CommentEntity comment) {
        // Kiểm tra xác thực người dùng
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Lấy thông tin người dùng hiện tại
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        // Kiểm tra comment có tồn tại không
        if (comment == null) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // Kiểm tra quyền xóa (chỉ tác giả mới được xóa)
        if (!comment.getAuthor().equals(userEntity)) {
            throw new AppException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        try {
            // Lấy tất cả comment để tìm comment con
            List<CommentEntity> allComments = commentRepository.findAll();
            // Xóa comment và các comment con
            deleteWithChildren(comment, allComments);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.COMMENT_DELETE_FAILED, "Xóa comment thất bại: " + e.getMessage());
        }
    }

    private void deleteWithChildren(CommentEntity comment, List<CommentEntity> allComments) {
        // Tìm tất cả comment con của comment hiện tại
        List<CommentEntity> children = allComments.stream()
                .filter(c -> c.getParentComment() != null && c.getParentComment().getId().equals(comment.getId()))
                .collect(Collectors.toList());

        // Xóa đệ quy từng comment con
        for (CommentEntity child : children) {
            deleteWithChildren(child, allComments);
        }

        // Xóa comment hiện tại sau khi đã xóa hết comment con
        commentRepository.delete(comment);
    }
}