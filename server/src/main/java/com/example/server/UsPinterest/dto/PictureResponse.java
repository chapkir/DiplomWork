package com.example.server.UsPinterest.dto;

public class PictureResponse {
    private Long id;
    private String imageUrl;
    private String fullhdImageUrl;
    private String thumbnailImageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer fullhdWidth;
    private Integer fullhdHeight;
    private Integer thumbnailWidth;
    private Integer thumbnailHeight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFullhdImageUrl() {
        return fullhdImageUrl;
    }

    public void setFullhdImageUrl(String fullhdImageUrl) {
        this.fullhdImageUrl = fullhdImageUrl;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
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

    public Integer getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(Integer thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public Integer getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(Integer thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }
} 