package com.example.server.UsPinterest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String bio;
    private boolean isBanned;
    private String createdAt;
    private String lastLoginAt;
} 