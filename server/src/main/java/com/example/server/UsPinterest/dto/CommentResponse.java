package com.example.server.UsPinterest.dto;

public class CommentResponse {
    private Long id;
    private String text;
    private String username;

    public CommentResponse() {}

    public CommentResponse(Long id, String text, String username) {
        this.id = id;
        this.text = text;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}