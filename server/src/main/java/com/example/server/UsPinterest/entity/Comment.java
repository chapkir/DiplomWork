package com.example.server.UsPinterest.entity;

import com.example.server.UsPinterest.model.Photo;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id")
    private Pin pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    // Конструкторы
    public Comment() {
        this.createdAt = LocalDateTime.now();
    }

    public Comment(String text, User user, Post post) {
        this.text = text;
        this.user = user;
        this.post = post;
        this.createdAt = LocalDateTime.now();
    }

    // Конструктор для Pin
    public Comment(String text, User user, Pin pin) {
        this.text = text;
        this.user = user;
        this.pin = pin;
        this.createdAt = LocalDateTime.now();
    }

    // Конструктор для Photo
    public Comment(String text, User user, Photo photo) {
        this.text = text;
        this.user = user;
        this.photo = photo;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Pin getPin() {
        return pin;
    }

    public void setPin(Pin pin) {
        this.pin = pin;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
} 