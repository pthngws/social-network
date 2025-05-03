package com.phithang.mysocialnetwork.service.Impl;

import com.cloudinary.Cloudinary;
import com.phithang.mysocialnetwork.dto.request.StoryRequestDto;
import com.phithang.mysocialnetwork.dto.response.StoryInteractionResponseDto;
import com.phithang.mysocialnetwork.dto.response.StoryResponseDto;
import com.phithang.mysocialnetwork.entity.*;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.MusicRepository;
import com.phithang.mysocialnetwork.repository.StoryInteractionRepository;
import com.phithang.mysocialnetwork.repository.StoryRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoryService implements IStoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private StoryInteractionRepository interactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return userEntity.getId();
    }

    @Override
    public StoryResponseDto createStory(StoryRequestDto request) {
        Long userId = getCurrentUserId();
        MultipartFile media = request.getMedia();

        // Validate file
        if (media == null || media.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media file is required");
        }
        String contentType = media.getContentType();
        if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image or video files are allowed");
        }

        // Upload to Cloudinary
        String mediaUrl;
        try {
            Map uploadResult = cloudinary.uploader().upload(media.getBytes(),
                    Map.of("resource_type", contentType.startsWith("video/") ? "video" : "image"));
            mediaUrl = (String) uploadResult.get("url");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload media: " + e.getMessage());
        }

        Story story = new Story();
        story.setUserId(userId);
        story.setContent(mediaUrl);
        story.setPostedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusHours(24));

        if (request.getMusicId() != null) {
            Optional<Music> music = musicRepository.findById(request.getMusicId());
            if (music.isPresent()) {
                story.setMusic(music.get());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Music not found");
            }
        }

        Story savedStory = storyRepository.save(story);
        return mapToStoryResponseDto(savedStory);
    }

    @Override
    public void deleteStory(Long storyId) {
        Long userId = getCurrentUserId();

        Optional<Story> story = storyRepository.findById(storyId);
        if (story.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found");
        }
        if (!story.get().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own story");
        }
        storyRepository.delete(story.get());
    }

    @Override
    public StoryResponseDto viewStory(Long storyId) {
        Long userId = getCurrentUserId();

        Optional<Story> story = storyRepository.findById(storyId);
        if (story.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found");
        }

        // Check if the user has already viewed the story
        boolean hasViewed = interactionRepository.existsByStoryIdAndUserIdAndInteractionType(
                storyId, userId, InteractionType.VIEW);
        if (!hasViewed) {
            // If not viewed, create a new view interaction
            StoryInteraction view = new StoryInteraction();
            view.setUserId(userId);
            view.setStory(story.get());
            view.setInteractionType(InteractionType.VIEW);
            view.setInteractedAt(LocalDateTime.now());
            interactionRepository.save(view);
        }
        // If already viewed, do not create a new interaction, just return the story
        return mapToStoryResponseDto(story.get());
    }

    @Override
    public Boolean reactStory(Long storyId, String reactionTypeStr) {
        Long userId = getCurrentUserId();

        // Convert String to Enum (case-insensitive)
        ReactionType reactionType;
        try {
            reactionType = ReactionType.valueOf(reactionTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reaction type: " + reactionTypeStr);
        }

        // Check if the story exists
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found"));

        // Check for existing view interaction
        Optional<StoryInteraction> existingViewOpt = interactionRepository.findByStoryIdAndUserIdAndInteractionType(
                storyId, userId, InteractionType.VIEW);

        if (existingViewOpt.isPresent()) {
            // If view exists, update the timestamp only
            StoryInteraction existingView = existingViewOpt.get();
            existingView.setInteractedAt(LocalDateTime.now());
            interactionRepository.save(existingView);
        } else {
            // If no view exists, create a new view interaction
            StoryInteraction newView = new StoryInteraction();
            newView.setUserId(userId);
            newView.setStory(story);
            newView.setInteractionType(InteractionType.VIEW);
            newView.setInteractedAt(LocalDateTime.now());
            interactionRepository.save(newView);
        }

        // Handle reaction (update or insert)
        Optional<StoryInteraction> existingReactionOpt = interactionRepository.findByStoryIdAndUserIdAndInteractionType(
                storyId, userId, InteractionType.REACTION);

        if (existingReactionOpt.isPresent()) {
            // If reaction exists, update reactionType and timestamp
            StoryInteraction existingReaction = existingReactionOpt.get();
            existingReaction.setReactionType(reactionType);
            existingReaction.setInteractedAt(LocalDateTime.now());
            interactionRepository.save(existingReaction);
        } else {
            // If no reaction exists, create a new reaction
            StoryInteraction newReaction = new StoryInteraction();
            newReaction.setUserId(userId);
            newReaction.setStory(story);
            newReaction.setInteractionType(InteractionType.REACTION);
            newReaction.setReactionType(reactionType);
            newReaction.setInteractedAt(LocalDateTime.now());
            interactionRepository.save(newReaction);
        }

        return true;
    }

    @Override
    public List<StoryInteractionResponseDto> getStoryInteractions(Long storyId) {
        Long userId = getCurrentUserId();

        Optional<Story> story = storyRepository.findById(storyId);
        if (story.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found");
        }
        if (!story.get().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view interactions of your own story");
        }

        List<StoryInteraction> interactions = interactionRepository.findByStoryId(storyId);
        return interactions.stream()
                .map(this::mapToStoryInteractionResponseDto)
                .collect(Collectors.toList());
    }

    private StoryResponseDto mapToStoryResponseDto(Story story) {
        UserEntity user = userRepository.findById(story.getUserId()).get();
        StoryResponseDto dto = new StoryResponseDto();
        dto.setId(story.getId());
        dto.setUserId(story.getUserId());
        dto.setFullName(user.getFirstname() + " " + user.getLastname());
        dto.setAvatar(user.getImageUrl());
        dto.setContent(story.getContent());
        dto.setMusicUrl(story.getMusic().getUrl());
        dto.setPostedAt(story.getPostedAt());
        dto.setExpiresAt(story.getExpiresAt());
        return dto;
    }

    private StoryInteractionResponseDto mapToStoryInteractionResponseDto(StoryInteraction interaction) {
        UserEntity user = userRepository.findById(interaction.getUserId()).get();
        StoryInteractionResponseDto dto = new StoryInteractionResponseDto();
        dto.setUserId(interaction.getUserId());
        dto.setAvatar(user.getImageUrl());
        dto.setFullName(user.getFirstname()+" " +user.getLastname());
        dto.setInteractionType(interaction.getInteractionType());
        dto.setReactionType(interaction.getReactionType());
        dto.setInteractedAt(interaction.getInteractedAt());
        return dto;
    }

    @Override
    public List<StoryResponseDto> findAll()
    {
        List<Story> storyList = storyRepository.findAll();
        List<StoryResponseDto> storyResponseDtoList = new ArrayList<>();
        for (Story story : storyList) {
            StoryResponseDto dto = mapToStoryResponseDto(story);
            storyResponseDtoList.add(dto);
        }
        return storyResponseDtoList;
    }



}