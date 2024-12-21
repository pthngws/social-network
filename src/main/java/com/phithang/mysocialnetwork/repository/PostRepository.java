package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PostRepository extends JpaRepository<PostEntity,Long> {
}
