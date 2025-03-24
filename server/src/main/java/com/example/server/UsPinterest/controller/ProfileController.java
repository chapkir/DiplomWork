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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

            // Получаем пины пользователя через репозиторий
            List<Pin> userPins = pinRepository.findByUser(user);
            List<PinResponse> pinResponses = userPins.stream()
                    .map(pin -> {
                        PinResponse pr = new PinResponse();
                        pr.setId(pin.getId());
                        pr.setImageUrl(pin.getImageUrl());
                        pr.setDescription(pin.getDescription());
                        pr.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);
                        pr.setIsLikedByCurrentUser(pin.getLikes().stream()
                                .anyMatch(like -> like.getUser().getId().equals(user.getId())));
                        pr.setComments(
                                pin.getComments().stream().map(comment -> {
                                    CommentResponse cr = new CommentResponse();
                                    cr.setId(comment.getId());
                                    cr.setText(comment.getText());
                                    cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                                    return cr;
                                }).collect(Collectors.toList())
                        );
                        return pr;
                    })
                    .collect(Collectors.toList());

            ProfileResponse response = new ProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setProfileImageUrl(user.getProfileImageUrl());
            response.setBio(user.getBio());
            response.setRegistrationDate(user.getRegistrationDate());
            response.setPins(pinResponses);
            response.setPinsCount(pinResponses.size());

            // Добавление количества подписчиков и подписок
            long followersCount = userRepository.countFollowersByUserId(user.getId());
            long followingCount = userRepository.countFollowingByUserId(user.getId());
            response.setFollowersCount((int) followersCount);
            response.setFollowingCount((int) followingCount);

            System.out.println("Профиль успешно сформирован и отправлен");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Ошибка при получении профиля: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Загрузка и обновление изображения профиля пользователя
     *
     * @param file файл изображения
     * @param authentication информация об аутентифицированном пользователе
     * @return ответ с обновленным профилем пользователя
     */
    @PostMapping("/image")
    public ResponseEntity<?> updateProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        logger.info("Получен запрос на обновление изображения профиля");

        try {
            if (authentication == null) {
                logger.error("Ошибка аутентификации: объект Authentication равен null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Необходима аутентификация"));
            }

            String username = authentication.getName();
            logger.info("Обработка запроса на загрузку изображения для пользователя: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

            // Проверяем, что файл не пустой и имеет правильный формат
            if (file.isEmpty()) {
                logger.error("Получен пустой файл");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Файл не должен быть пустым"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.error("Неподдерживаемый формат файла: {}", contentType);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Поддерживаются только файлы изображений"));
            }

            // Обновляем изображение профиля
            User updatedUser = userService.updateProfileImage(user.getId(), file);

            // Формируем ответ
            ProfileResponse response = new ProfileResponse();
            response.setId(updatedUser.getId());
            response.setUsername(updatedUser.getUsername());
            response.setEmail(updatedUser.getEmail());
            response.setProfileImageUrl(updatedUser.getProfileImageUrl());

            logger.info("Изображение профиля успешно обновлено для пользователя: {}", username);
            return ResponseEntity.ok(ApiResponse.success(response, "Изображение профиля успешно обновлено"));

        } catch (ResourceNotFoundException e) {
            logger.error("Пользователь не найден: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            logger.error("Ошибка при обновлении изображения профиля: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Не удалось обновить изображение профиля: " + e.getMessage()));
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