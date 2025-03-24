package com.phithang.mysocialnetwork.repository;


import com.phithang.mysocialnetwork.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    void deleteByPostId(Long postId);
    @Modifying
    @Query("DELETE FROM CommentEntity c WHERE c.parentComment.id = :parentId")
    void deleteByParentCommentId(@Param("parentId") Long parentId);
}
