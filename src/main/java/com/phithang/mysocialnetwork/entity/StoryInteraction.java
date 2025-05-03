package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_interaction")
@Data
public class StoryInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // ID của người tương tác

    @ManyToOne
    @JoinColumn(name = "story_id", nullable = false)
    private Story story; // Story được tương tác

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType interactionType; // Loại tương tác (VIEW hoặc REACTION)

    @Enumerated(EnumType.STRING)
    @Column
    private ReactionType reactionType; // Loại cảm xúc (null nếu là VIEW)

    @Column(name = "interacted_at", nullable = false)
    private LocalDateTime interactedAt = LocalDateTime.now();
}