package com.example.server.UsPinterest.dto;

public class FeedbackRequest {
    private String whatLiked;
    private String whatDisliked;
    private String recommendations;

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
} 