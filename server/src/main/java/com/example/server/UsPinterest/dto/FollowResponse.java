package com.example.server.UsPinterest.dto;

import java.time.LocalDateTime;

/**
 * DTO для представления информации о подписке пользователя
 */
public class FollowResponse {
    private Long id;
    private Long followerId;
    private String followerUsername;
    private String followerProfileImageUrl;
    private Long followingId;
    private String followingUsername;
    private String followingProfileImageUrl;
    private LocalDateTime createdAt;

    public FollowResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public String getFollowerUsername() {
        return followerUsername;
    }

    public void setFollowerUsername(String followerUsername) {
        this.followerUsername = followerUsername;
    }

    public String getFollowerProfileImageUrl() {
        return followerProfileImageUrl;
    }

    public void setFollowerProfileImageUrl(String followerProfileImageUrl) {
        this.followerProfileImageUrl = followerProfileImageUrl;
    }

    public Long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Long followingId) {
        this.followingId = followingId;
    }

    public String getFollowingUsername() {
        return followingUsername;
    }

    public void setFollowingUsername(String followingUsername) {
        this.followingUsername = followingUsername;
    }

    public String getFollowingProfileImageUrl() {
        return followingProfileImageUrl;
    }

    public void setFollowingProfileImageUrl(String followingProfileImageUrl) {
        this.followingProfileImageUrl = followingProfileImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}