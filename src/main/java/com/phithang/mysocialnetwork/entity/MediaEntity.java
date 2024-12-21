package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medias")
public class MediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url; // URL hoặc đường dẫn đến file ảnh/video

    @Column(nullable = false)
    private String type; // "image" hoặc "video"
}