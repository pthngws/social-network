package com.phithang.mysocialnetwork.dto.response;

import com.phithang.mysocialnetwork.entity.InteractionType;
import com.phithang.mysocialnetwork.entity.ReactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoryInteractionResponseDto {
    private Long userId;
    private String fullName;
    private String avatar;
    private InteractionType interactionType;
    private ReactionType reactionType;
    private LocalDateTime interactedAt;
}