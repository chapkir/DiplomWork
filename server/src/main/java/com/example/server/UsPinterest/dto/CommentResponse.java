package com.example.server.UsPinterest.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String text;
    private String username;
    private LocalDateTime createdAt;
    private String userProfileImageUrl;

    public CommentResponse() {}

    public CommentResponse(Long id, String text, String username, LocalDateTime createdAt, String userProfileImageUrl) {
        this.id = id;
        this.text = text;
        this.username = username;
        this.createdAt = createdAt;
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }
}