package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity,Long> {

    List<PostEntity> findAllByAuthor(UserEntity author);
}
