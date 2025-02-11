package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.entity.CommentEntity;

public interface ICommentService {
    CommentEntity findById(Long id);

    boolean delete(CommentEntity comment);
}
