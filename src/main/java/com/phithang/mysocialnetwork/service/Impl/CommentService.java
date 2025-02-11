package com.phithang.mysocialnetwork.service.Impl;


import com.phithang.mysocialnetwork.entity.CommentEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
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
    public CommentEntity findById(Long id)
    {
        return commentRepository.findById(id).get();
    }

    @Override
    public boolean delete(CommentEntity comment)
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if(comment.getAuthor().equals(userEntity))
        {
            commentRepository.delete(comment);
            return true;
        }
        return false;
    }


}
