package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.StoryRequestDto;
import com.phithang.mysocialnetwork.dto.response.StoryInteractionResponseDto;
import com.phithang.mysocialnetwork.dto.response.StoryResponseDto;
import com.phithang.mysocialnetwork.entity.Story;
import com.phithang.mysocialnetwork.entity.StoryInteraction;

import java.util.List;

public interface IStoryService {
    StoryResponseDto createStory(StoryRequestDto request);
    void deleteStory(Long storyId);
    StoryResponseDto viewStory(Long storyId);
    List<StoryInteractionResponseDto> getStoryInteractions(Long storyId);

    List<StoryResponseDto> findAll();

    Boolean reactStory(Long storyId, String reactionTypeStr);
}