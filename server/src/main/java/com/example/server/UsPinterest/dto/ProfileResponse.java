package com.example.server.UsPinterest.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String profileImageUrl;
    private LocalDateTime registrationDate;
    private String firstName;
    private String city;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthDate;
    private String gender;
    private List<PinResponse> pins;
    private List<PostResponse> posts;
    private List<BoardResponse> boards;
    private int pinsCount;
    private int postsCount;
    private int followersCount;
    private int followingCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<PinResponse> getPins() {
        return pins;
    }

    public void setPins(List<PinResponse> pins) {
        this.pins = pins;
    }

    public List<PostResponse> getPosts() {
        return posts;
    }

    public void setPosts(List<PostResponse> posts) {
        this.posts = posts;
    }

    public List<BoardResponse> getBoards() {
        return boards;
    }

    public void setBoards(List<BoardResponse> boards) {
        this.boards = boards;
    }

    public int getPinsCount() {
        return pinsCount;
    }

    public void setPinsCount(int pinsCount) {
        this.pinsCount = pinsCount;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}
