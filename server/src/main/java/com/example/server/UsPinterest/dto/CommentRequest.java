package com.example.server.UsPinterest.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {
    @NotBlank(message = "Комментарий не может быть пустым")
    private String text;

    public CommentRequest() {}

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}