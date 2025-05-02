package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.PostReactionEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReactionEntity, Long> {
    Optional<PostReactionEntity> findByPostAndUser(PostEntity post, UserEntity user);
}