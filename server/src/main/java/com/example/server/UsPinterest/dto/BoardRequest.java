package com.example.server.UsPinterest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания и обновления доски
 */
public class BoardRequest {
    @NotBlank(message = "Название доски не может быть пустым")
    @Size(max = 100, message = "Название доски не может быть длиннее 100 символов")
    private String title;

    @Size(max = 500, message = "Описание доски не может быть длиннее 500 символов")
    private String description;

    public BoardRequest() {
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
}