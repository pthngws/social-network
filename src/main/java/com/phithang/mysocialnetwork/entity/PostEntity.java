package com.phithang.mysocialnetwork.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private UserEntity author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostMediaEntity> postMedia;

    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<UserEntity> likedBy;

    private java.time.LocalDateTime timestamp;

    @Override
    public String toString() {
        return "PostEntity{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", authorId=" + author.getId() +
                ", commentCount=" + (comments != null ? comments.size() : 0) +
                ", likedByCount=" + (likedBy != null ? likedBy.size() : 0) +
                ", timestamp=" + timestamp +
                '}';
    }

}
