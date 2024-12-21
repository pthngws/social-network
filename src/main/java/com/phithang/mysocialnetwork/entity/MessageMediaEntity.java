package com.phithang.mysocialnetwork.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "message_media")
public class MessageMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @ManyToOne
    @JoinColumn(name = "media_id", nullable = false)
    private MediaEntity media;

    // Getters và Setters
}
