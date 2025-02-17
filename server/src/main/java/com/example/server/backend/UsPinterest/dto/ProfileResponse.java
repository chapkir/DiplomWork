package com.example.diplomwork.backend.UsPinterest.dto;

import java.util.List;

public class ProfileResponse {
    private String username;
    private String email;
    private List<PinResponse> pins;

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

    public List<PinResponse> getPins() {
        return pins;
    }

    public void setPins(List<PinResponse> pins) {
        this.pins = pins;
    }
}
