package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "post_media")
public class PostMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)  // Thêm CascadeType.ALL ở đây
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(cascade = CascadeType.ALL)  // Thêm CascadeType.ALL ở đây
    @JoinColumn(name = "media_id", nullable = false)
    private MediaEntity media;

    // Getters và Setters
}
