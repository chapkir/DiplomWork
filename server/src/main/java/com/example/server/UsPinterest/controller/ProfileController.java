package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.ApiResponse;
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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;
    private final BoardService boardService;
    private final PinService pinService;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final LikeRepository likeRepository;

    @GetMapping
    public ResponseEntity<?> getProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
            }

            User user = userService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            ProfileResponse response = new ProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setProfileImageUrl(user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
            response.setRegistrationDate(user.getRegistrationDate());
            response.setBoards(user.getBoards());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении профиля: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении профиля: " + e.getMessage());
        }
    }

    /**
     * Загрузка и обновление изображения профиля пользователя
     *
     * @param file файл изображения
     * @return ответ с обновленным профилем пользователя
     */
    @PostMapping("/image")
    public ResponseEntity<?> updateProfileImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Файл не выбран");
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Файл должен быть изображением");
            }

            User user = userService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
            }

            User updatedUser = userService.updateProfileImage(user.getId(), file);
            if (updatedUser == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Не удалось обновить изображение профиля");
            }

            return ResponseEntity.ok(updatedUser.getProfileImageUrl());
        } catch (IOException e) {
            logger.error("Ошибка при загрузке изображения профиля: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке изображения: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при обновлении изображения профиля: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при обновлении изображения профиля");
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