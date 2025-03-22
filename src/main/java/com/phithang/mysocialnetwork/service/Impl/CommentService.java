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
    public boolean delete(CommentEntity comment) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        if (comment == null) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getAuthor().equals(userEntity)) {
            throw new AppException(ErrorCode.COMMENT_UNAUTHORIZED);
        }
        try {
            commentRepository.delete(comment);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.COMMENT_DELETE_FAILED);
        }
    }
}