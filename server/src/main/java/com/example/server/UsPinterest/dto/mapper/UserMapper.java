package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Маппер для преобразования между сущностью User и DTO
 */
@Component
public class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Преобразует User в объект ProfileResponse
     *
     * @param user сущность пользователя
     * @return объект ProfileResponse
     */
    public ProfileResponse toProfileDto(User user) {
        if (user == null) {
            return null;
        }

        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setRegistrationDate(user.getRegistrationDate());

        // По умолчанию, пины не загружаем в маппере,
        // их нужно загружать отдельно через PinService
        response.setPins(new ArrayList<>());

        return response;
    }

    /**
     * Создает сущность User из данных RegisterRequest
     *
     * @param request запрос на регистрацию
     * @return сущность User
     */
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

    /**
     * Обновляет данные пользователя из ProfileResponse
     *
     * @param user     существующий пользователь
     * @param profile  данные профиля для обновления
     * @return обновленная сущность User
     */
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
}