package com.example.server.UsPinterest.dto;

public class PinFullHdResponse {
    private Long id;
    private String fullhdImageUrl;
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