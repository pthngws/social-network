package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.PostMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMediaEntity,Long>
{
    List<PostMediaEntity> findByPostId(Long postId);
}
