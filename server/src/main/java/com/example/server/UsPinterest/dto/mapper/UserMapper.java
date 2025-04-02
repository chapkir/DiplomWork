package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;


    public ProfileResponse toProfileDto(User user) {
        if (user == null) {
            return null;
        }

        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());


        String profileImageUrl = user.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            response.setProfileImageUrl(fileStorageService.updateImageUrl(profileImageUrl));
        }

        response.setRegistrationDate(user.getRegistrationDate());

        response.setPins(new ArrayList<>());

        return response;
    }

    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());

        return user;
    }

    public User updateFromProfile(User user, ProfileResponse profile) {
        if (user == null || profile == null) {
            return user;
        }

        if (profile.getBio() != null) {
            user.setBio(profile.getBio());
        }

        if (profile.getProfileImageUrl() != null) {
            user.setProfileImageUrl(profile.getProfileImageUrl());
        }

        return user;
    }

    public List<ProfileResponse> mapUsersToProfileResponses(List<User> users) {
        return users.stream()
                .map(this::toProfileDto)
                .collect(Collectors.toList());
    }
}