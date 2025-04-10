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
import com.example.server.UsPinterest.service.FileStorageService;

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
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    @Autowired
    private FileStorageService fileStorageService;

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

            // Получаем пины пользователя
            List<Pin> userPins = pinRepository.findByUser(user);
            List<PinResponse> pinResponses = userPins.stream()
                    .map(pin -> {
                        PinResponse pr = new PinResponse();
                        pr.setId(pin.getId());
                        pr.setImageUrl(pin.getImageUrl());
                        pr.setDescription(pin.getDescription());
                        pr.setLikesCount(pin.getLikes().size());
                        pr.setUserId(user.getId());
                        pr.setUsername(user.getUsername());
                        pr.setUserProfileImageUrl(user.getProfileImageUrl());
                        pr.setCreatedAt(pin.getCreatedAt());
                        pr.setIsLikedByCurrentUser(pin.getLikes().stream()
                                .anyMatch(like -> like.getUser().getId().equals(user.getId())));

                        if (pin.getBoard() != null) {
                            pr.setBoardId(pin.getBoard().getId());
                            pr.setBoardTitle(pin.getBoard().getTitle());
                        }

                        return pr;
                    })
                    .collect(Collectors.toList());
            response.setPins(pinResponses);
            response.setPinsCount(pinResponses.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении профиля", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении профиля: " + e.getMessage());
        }
    }

    @PostMapping(value = {"/image", "/avatar"})
    public ResponseEntity<?> updateProfileImage(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Необходима авторизация"));
        }

        String username = authentication.getName();
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String imageUrl = fileStorageService.storeProfileImage(file, user.getId());

            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Collections.singletonMap("profileImageUrl", imageUrl));
        } catch (IOException e) {
            logger.error("Ошибка при загрузке изображения профиля", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Ошибка при загрузке изображения: " + e.getMessage()));
        }
    }

    @GetMapping("/liked-pins")
    public ResponseEntity<?> getLikedPins(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

            List<Like> likes = likeRepository.findByUser(user);
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

            return ResponseEntity.ok(pinResponses);
        } catch (Exception e) {
            logger.error("Ошибка при получении лайкнутых пинов", e);
            return ResponseEntity.status(500)
                    .body("Ошибка при получении лайкнутых пинов: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfileById(@PathVariable Long userId, Authentication authentication) {
        try {
            // Проверяем аутентификацию
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
            }

            // Находим пользователя по ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID: " + userId + " не найден"));

            // Создаем объект ответа
            ProfileResponse response = new ProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setProfileImageUrl(user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
            response.setRegistrationDate(user.getRegistrationDate());
            response.setBoards(user.getBoards());

            // Получаем пины пользователя
            List<Pin> userPins = pinRepository.findByUser(user);
            List<PinResponse> pinResponses = userPins.stream()
                    .map(pin -> {
                        PinResponse pr = new PinResponse();
                        pr.setId(pin.getId());
                        pr.setImageUrl(pin.getImageUrl());
                        pr.setDescription(pin.getDescription());
                        pr.setLikesCount(pin.getLikes().size());
                        pr.setUserId(user.getId());
                        pr.setUsername(user.getUsername());
                        pr.setUserProfileImageUrl(user.getProfileImageUrl());
                        pr.setCreatedAt(pin.getCreatedAt());

                        // Проверяем, лайкнул ли текущий пользователь этот пин
                        User currentUser = userService.getCurrentUser();
                        pr.setIsLikedByCurrentUser(pin.getLikes().stream()
                                .anyMatch(like -> like.getUser().getId().equals(currentUser.getId())));

                        if (pin.getBoard() != null) {
                            pr.setBoardId(pin.getBoard().getId());
                            pr.setBoardTitle(pin.getBoard().getTitle());
                        }

                        return pr;
                    })
                    .collect(Collectors.toList());
            response.setPins(pinResponses);
            response.setPinsCount(pinResponses.size());

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.error("Ошибка при получении профиля по ID", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь с ID: " + userId + " не найден");
        } catch (Exception e) {
            logger.error("Ошибка при получении профиля по ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении профиля: " + e.getMessage());
        }
    }
}