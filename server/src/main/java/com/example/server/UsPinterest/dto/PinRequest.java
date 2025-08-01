package com.example.server.UsPinterest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import com.example.server.UsPinterest.validation.Step;
import java.util.List;

public class PinRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "URL изображения не может быть пустым")
    private String imageUrl;

    @Size(max = 500, message = "Описание не может быть длиннее 500 символов")
    private String description;

    private Long boardId;
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    @Step(value = 0.5, message = "Rating must be in increments of 0.5")
    private Double rating;

    private List<String> tags;

    public PinRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Long getBoardId() {
        return boardId;
    }
    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
} 