package com.phithang.mysocialnetwork.repository;


import com.phithang.mysocialnetwork.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    void deleteByPostId(Long postId);
}
