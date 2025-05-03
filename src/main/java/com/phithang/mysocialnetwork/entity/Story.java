package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "story")
@Data
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // ID của người đăng story

    @Column
    private String content; // Nội dung story (văn bản, URL ảnh/video)

    @ManyToOne
    @JoinColumn(name = "music_id", nullable = true)
    private Music music; // Nhạc gắn với story (có thể null)

    @Column(name = "posted_at", nullable = false, updatable = false)
    private LocalDateTime postedAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời gian story hết hạn (thường là 24h sau khi đăng)

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StoryInteraction> interactions; // Danh sách tương tác (xem và thả cảm xúc)
}