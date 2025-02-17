package com.example.diplomwork.backend.UsPinterest.entity;//package com.pinterest.clone.entity;
//
//import com.pinterest.clone.model.User;
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(name = "pins")
//public class Pin {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String imageUrl;
//
//    private String description;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt;
//
//    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL)
//    private Set<Comment> comments = new HashSet<>();
//
//    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL)
//    private Set<Like> likes = new HashSet<>();
//
//    // Геттеры и сеттеры
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getImageUrl() {
//        return imageUrl;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public Set<Comment> getComments() {
//        return comments;
//    }
//
//    public void setComments(Set<Comment> comments) {
//        this.comments = comments;
//    }
//
//    public Set<Like> getLikes() {
//        return likes;
//    }
//
//    public void setLikes(Set<Like> likes) {
//        this.likes = likes;
//    }
//}