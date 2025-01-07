package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // Người nhận thông báo

    private String content; // Nội dung thông báo

    private int isread = 0; // Trạng thái đã đọc hay chưa

    private java.time.LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = true) // Thông báo có thể liên quan đến bài viết
    private PostEntity post;
}
