package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.ApiResponse;
import com.example.server.UsPinterest.dto.EditProfileRequest;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PostRepository;
import com.example.server.UsPinterest.service.BoardService;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.service.PostService;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.service.FileStorageService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;

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
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Timed(value = "profile.controller", description = "Metrics for ProfileController endpoints")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;
    private final BoardService boardService;
    private final PinService pinService;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private PostService postService;
    @Autowired
    private Counter profileImageUploadCounter;
    @Autowired
    private DistributionSummary fileUploadSizeSummary;

    @GetMapping
    @Transactional(readOnly = true)
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
            response.setFirstName(user.getFirstName());
            response.setCity(user.getCity());
            response.setBirthDate(user.getBirthDate());
            response.setGender(user.getGender());
            response.setBoards(boardService.getBoardsByUserId(user.getId(), false));

            // Получаем пины пользователя с сортировкой по дате создания (сначала новые)
            List<Pin> userPins = pinRepository.findByUserOrderByCreatedAtDesc(user);
            List<PinResponse> pinResponses = userPins.stream()
                    .map(pin -> pinService.convertToPinResponse(pin, user))
                    .collect(Collectors.toList());
            response.setPins(pinResponses);
            response.setPinsCount(pinResponses.size());

            // Получаем посты пользователя
            List<Post> userPosts = postRepository.findByUserOrderByCreatedAtDesc(user);
            List<PostResponse> postResponses = userPosts.stream()
                    .map(post -> postService.convertToPostResponse(post, user))
                    .collect(Collectors.toList());
            response.setPosts(postResponses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении профиля", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении профиля: " + e.getMessage());
        }
    }

    @PostMapping(value = {"/image", "/avatar"})
    public ResponseEntity<?> updateProfileImage(@RequestParam("file") MultipartFile file) {
        profileImageUploadCounter.increment();
        fileUploadSizeSummary.record(file.getSize());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Необходима авторизация"));
        }

        String username = authentication.getName();
        try {
            User user = userService.getUserWithCollectionsByUsername(username);

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
    @Transactional(readOnly = true)
    public ResponseEntity<?> getLikedPins(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            User user = userService.getUserWithCollectionsByUsername(username);

            // Используем метод с сортировкой (сначала новые лайки)
            List<Like> likes = likeRepository.findByUserOrderByIdDesc(user);
            List<PinResponse> pinResponses = new ArrayList<>();

            for (Like like : likes) {
                Pin pin = like.getPin();
                if (pin != null) {
                    pinResponses.add(pinService.convertToPinResponse(pin, user));
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
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProfileById(@PathVariable Long userId, Authentication authentication) {
        try {
            User targetUser = userService.getUserWithCollections(userId);

            ProfileResponse response = new ProfileResponse();
            response.setId(targetUser.getId());
            response.setUsername(targetUser.getUsername());
            response.setEmail(targetUser.getEmail());
            response.setProfileImageUrl(targetUser.getProfileImageUrl() != null ? targetUser.getProfileImageUrl() : "");
            response.setRegistrationDate(targetUser.getRegistrationDate());
            response.setFirstName(targetUser.getFirstName());
            response.setCity(targetUser.getCity());
            response.setBirthDate(targetUser.getBirthDate());
            response.setGender(targetUser.getGender());
            response.setBoards(boardService.getBoardsByUserId(targetUser.getId(), false));

            // Получаем пины пользователя с сортировкой по дате создания (сначала новые)
            List<Pin> userPins = pinRepository.findByUserOrderByCreatedAtDesc(targetUser);

            // Если пользователь авторизован, получаем текущего пользователя для проверки лайков
            final User currentUser = authentication != null
                    ? userService.getUserWithCollectionsByUsername(authentication.getName())
                    : null;

            // Преобразуем пины с учетом контекста текущего пользователя (для отображения лайков)
            List<PinResponse> pinResponses = userPins.stream()
                    .map(pin -> pinService.convertToPinResponse(pin, currentUser))
                    .collect(Collectors.toList());

            response.setPins(pinResponses);
            response.setPinsCount(pinResponses.size());

            // Получаем посты пользователя
            List<Post> userPosts = postRepository.findByUserOrderByCreatedAtDesc(targetUser);
            List<PostResponse> postResponses = userPosts.stream()
                    .map(post -> postService.convertToPostResponse(post, currentUser))
                    .collect(Collectors.toList());
            response.setPosts(postResponses);

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Пользователь с id {} не найден", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        } catch (Exception e) {
            logger.error("Ошибка при получении профиля пользователя с id {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении профиля: " + e.getMessage());
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editProfile(@RequestBody EditProfileRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
            }

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            User updatedUser = userService.editProfile(currentUser.getId(), request);

            // Формируем ответ
            ProfileResponse response = new ProfileResponse();
            response.setId(updatedUser.getId());
            response.setUsername(updatedUser.getUsername());
            response.setEmail(updatedUser.getEmail());
            response.setProfileImageUrl(updatedUser.getProfileImageUrl() != null ? updatedUser.getProfileImageUrl() : "");
            response.setRegistrationDate(updatedUser.getRegistrationDate());
            response.setFirstName(updatedUser.getFirstName());
            response.setBio(updatedUser.getBio());
            response.setCity(updatedUser.getCity());
            response.setGender(updatedUser.getGender());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при редактировании профиля", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при редактировании профиля: " + e.getMessage());
        }
    }
}