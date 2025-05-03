package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByExpiresAtAfter(LocalDateTime now);
}
