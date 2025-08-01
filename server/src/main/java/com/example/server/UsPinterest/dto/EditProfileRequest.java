package com.example.server.UsPinterest.dto;

import jakarta.validation.constraints.Email;

public class EditProfileRequest {

    private String firstName;

    @Email(message = "Неверный формат email")
    private String email;

    private String gender;

    private String bio;

    private String city;

    public EditProfileRequest() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}