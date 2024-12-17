package com.phithang.mysocialnetwork.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friendships")
public class FriendshipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private UserEntity user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private UserEntity user2;

    private String status; // "PENDING", "ACCEPTED", "REJECTED"

    private java.time.LocalDateTime requestTimestamp;
}
