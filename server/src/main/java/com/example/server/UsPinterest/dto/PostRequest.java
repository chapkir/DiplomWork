package com.example.server.UsPinterest.dto;

public class PostRequest {
    private String text;
    private String imageUrl;

    public PostRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}