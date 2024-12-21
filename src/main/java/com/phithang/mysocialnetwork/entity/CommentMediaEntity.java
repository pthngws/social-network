package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comment_media")
public class CommentMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment;

    @ManyToOne
    @JoinColumn(name = "media_id", nullable = false)
    private MediaEntity media;

    // Getters và Setters
}
