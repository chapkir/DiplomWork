package com.example.diplomwork.backend.UsPinterest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PinRequest {

    @NotBlank(message = "URL изображения не может быть пустым")
    private String imageUrl;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    
    @NotNull(message = "Выбор доски обязателен")
    private Long boardId;

    public PinRequest() {}

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
} 