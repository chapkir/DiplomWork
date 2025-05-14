package com.example.server.UsPinterest.model;

import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.entity.Like;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.server.UsPinterest.model.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.CascadeType;

@Entity
@Table(name = "pins", indexes = {
        @Index(name = "idx_pins_description", columnList = "description"),
        @Index(name = "idx_pins_board", columnList = "board_id"),
        @Index(name = "idx_pins_user", columnList = "user_id"),
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false)
    private String title;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "board_id")
    @JsonIgnoreProperties("pins")
    private Board board;

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "pin_tags",
               joinColumns = @JoinColumn(name = "pin_id"),
               inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "email", "boards", "comments", "likes", "registrationDate", "profileImageUrl", "bio"})
    private User user;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "rating")
    private Double rating;

    // Поля для Full HD и миниатюрных изображений
    @Column(length = 1000, name = "fullhd_image_url")
    private String fullhdImageUrl;

    @Column(name = "fullhd_width")
    private Integer fullhdWidth;

    @Column(name = "fullhd_height")
    private Integer fullhdHeight;

    @Column(length = 1000, name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Picture> pictures = new ArrayList<>();

    public Pin() {}

    public Pin(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getFullhdImageUrl() { return fullhdImageUrl; }
    public void setFullhdImageUrl(String fullhdImageUrl) { this.fullhdImageUrl = fullhdImageUrl; }

    public Integer getFullhdWidth() { return fullhdWidth; }
    public void setFullhdWidth(Integer fullhdWidth) { this.fullhdWidth = fullhdWidth; }

    public Integer getFullhdHeight() { return fullhdHeight; }
    public void setFullhdHeight(Integer fullhdHeight) { this.fullhdHeight = fullhdHeight; }

    public String getThumbnailImageUrl() { return thumbnailImageUrl; }
    public void setThumbnailImageUrl(String thumbnailImageUrl) { this.thumbnailImageUrl = thumbnailImageUrl; }

    public Integer getThumbnailWidth() { return thumbnailWidth; }
    public void setThumbnailWidth(Integer thumbnailWidth) { this.thumbnailWidth = thumbnailWidth; }

    public Integer getThumbnailHeight() { return thumbnailHeight; }
    public void setThumbnailHeight(Integer thumbnailHeight) { this.thumbnailHeight = thumbnailHeight; }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }
} 