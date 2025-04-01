package com.example.server.UsPinterest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PinRequest {

    @NotBlank(message = "URL изображения не может быть пустым")
    private String imageUrl;

    @Size(max = 500, message = "Описание не может быть длиннее 500 символов")
    private String description;

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