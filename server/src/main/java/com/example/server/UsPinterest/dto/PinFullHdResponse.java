package com.example.server.UsPinterest.dto;

public class PinFullHdResponse {
    private Long id;
    private String fullhdImageUrl;
    private Long userId;
    private String username;
    private String userAvatar;
    private Double aspectRatio;
    private Integer fullhdWidth;
    private Integer fullhdHeight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullhdImageUrl() {
        return fullhdImageUrl;
    }

    public void setFullhdImageUrl(String fullhdImageUrl) {
        this.fullhdImageUrl = fullhdImageUrl;
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

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(Double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Integer getFullhdWidth() {
        return fullhdWidth;
    }

    public void setFullhdWidth(Integer fullhdWidth) {
        this.fullhdWidth = fullhdWidth;
    }

    public Integer getFullhdHeight() {
        return fullhdHeight;
    }

    public void setFullhdHeight(Integer fullhdHeight) {
        this.fullhdHeight = fullhdHeight;
    }
}