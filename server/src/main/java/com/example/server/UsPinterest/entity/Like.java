package com.example.server.UsPinterest.entity;

import com.example.server.UsPinterest.model.Photo;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "email", "boards", "comments", "likes", "registrationDate", "profileImageUrl", "bio"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "photo_id", nullable = true)
    private Photo photo;

    @ManyToOne
    @JoinColumn(name = "pin_id")
    @JsonIgnoreProperties({"likes", "comments", "board", "user"})
    private Pin pin;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Pin getPin() {
        return pin;
    }

    public void setPin(Pin pin) {
        this.pin = pin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 