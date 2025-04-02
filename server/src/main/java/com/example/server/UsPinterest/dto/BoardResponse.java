package com.example.server.UsPinterest.dto;

import java.util.List;

public class BoardResponse {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private String username;
    private String userProfileImageUrl;
    private int pinsCount;
    private List<PinResponse> pins;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public int getPinsCount() {
        return pinsCount;
    }

    public void setPinsCount(int pinsCount) {
        this.pinsCount = pinsCount;
    }

    public List<PinResponse> getPins() {
        return pins;
    }

    public void setPins(List<PinResponse> pins) {
        this.pins = pins;
    }
}