package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.InteractionType;
import com.phithang.mysocialnetwork.entity.StoryInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryInteractionRepository extends JpaRepository<StoryInteraction, Long> {
    boolean existsByStoryIdAndUserIdAndInteractionType(Long storyId, Long userId, InteractionType interactionType);
    Optional<StoryInteraction> findByStoryIdAndUserIdAndInteractionType(Long storyId, Long userId, InteractionType interactionType);
    List<StoryInteraction> findByStoryId(Long storyId);
}