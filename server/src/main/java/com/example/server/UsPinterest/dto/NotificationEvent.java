package com.example.server.UsPinterest.dto;

public class NotificationEvent {
    public enum Type {
        LIKE, COMMENT
    }

    private Type type;
    private Long senderId;
    private Long pinId;
    private String commentText;

    public NotificationEvent() {
    }

    public NotificationEvent(Type type, Long senderId, Long pinId, String commentText) {
        this.type = type;
        this.senderId = senderId;
        this.pinId = pinId;
        this.commentText = commentText;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getPinId() {
        return pinId;
    }

    public void setPinId(Long pinId) {
        this.pinId = pinId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}