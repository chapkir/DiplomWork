package com.example.server.UsPinterest.entity;

import com.example.server.UsPinterest.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "what_liked")
    private String whatLiked;

    @Column(name = "what_disliked")
    private String whatDisliked;

    @Column(name = "recommendations")
    private String recommendations;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWhatLiked() {
        return whatLiked;
    }

    public void setWhatLiked(String whatLiked) {
        this.whatLiked = whatLiked;
    }

    public String getWhatDisliked() {
        return whatDisliked;
    }

    public void setWhatDisliked(String whatDisliked) {
        this.whatDisliked = whatDisliked;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 