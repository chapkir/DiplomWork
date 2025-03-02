package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.service.BoardService;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.entity.Like;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserService userService;
    private final BoardService boardService;
    private final PinService pinService;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final LikeRepository likeRepository;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        System.out.println("Получен запрос на профиль пользователя");

        try {
            if (authentication == null) {
                System.err.println("Ошибка: Authentication объект равен null");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            System.out.println("Запрос профиля для пользователя: " + username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        System.err.println("Пользователь не найден: " + username);
                        return new ResourceNotFoundException("Пользователь не найден");
                    });

            System.out.println("Пользователь найден: " + user.getUsername());

            // Check if user has liked pins and fetch them
            List<Pin> likedPins = pinRepository.findByUser(user);
            System.out.println("Найдено пинов пользователя: " + likedPins.size());

            List<PinResponse> pinResponses = likedPins.stream().map(pin -> {
                PinResponse pr = new PinResponse();
                pr.setId(pin.getId());
                pr.setImageUrl(pin.getImageUrl());
                pr.setDescription(pin.getDescription());
                return pr;
            }).collect(Collectors.toList());

            ProfileResponse profileResponse = new ProfileResponse();
            profileResponse.setUsername(user.getUsername());
            profileResponse.setEmail(user.getEmail());
            profileResponse.setPins(pinResponses);

            System.out.println("Профиль успешно сформирован и отправлен");
            return ResponseEntity.ok(profileResponse);
        } catch (Exception e) {
            System.err.println("Ошибка при получении профиля: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/liked-pins")
    public ResponseEntity<?> getLikedPins(Authentication authentication) {
        System.out.println("Получен запрос на получение лайкнутых пинов");

        try {
            if (authentication == null) {
                System.err.println("Ошибка: Authentication объект равен null");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            System.out.println("Запрос лайкнутых пинов для пользователя: " + username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        System.err.println("Пользователь не найден: " + username);
                        return new ResourceNotFoundException("Пользователь не найден");
                    });

            System.out.println("Пользователь найден, ID: " + user.getId());

            List<Like> likes = likeRepository.findByUser(user);
            System.out.println("Найдено лайков: " + likes.size());

            List<PinResponse> pinResponses = new ArrayList<>();
            for (Like like : likes) {
                Pin pin = like.getPin();
                if (pin != null) {
                    PinResponse pr = new PinResponse();
                    pr.setId(pin.getId());
                    pr.setImageUrl(pin.getImageUrl());
                    pr.setDescription(pin.getDescription());
                    pr.setLikesCount(pin.getLikes().size());
                    pinResponses.add(pr);
                }
            }

            System.out.println("Подготовлено пинов для ответа: " + pinResponses.size());
            return ResponseEntity.ok(pinResponses);

        } catch (Exception e) {
            System.err.println("Ошибка при получении лайкнутых пинов: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Ошибка при получении лайкнутых пинов: " + e.getMessage());
        }
    }
} 