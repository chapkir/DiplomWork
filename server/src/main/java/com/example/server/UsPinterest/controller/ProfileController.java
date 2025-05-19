package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.EditProfileRequest;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.model.Picture;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PostRepository;
import com.example.server.UsPinterest.repository.FollowRepository;
import com.example.server.UsPinterest.repository.LocationRepository;
import com.example.server.UsPinterest.model.Location;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final LocationRepository locationRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final FileStorageService fileStorageService;
    private final PostService postService;
    private final Counter profileImageUploadCounter;
    private final DistributionSummary fileUploadSizeSummary;

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
            response.setBio(user.getBio());
            response.setProfileImageUrl(user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
            response.setRegistrationDate(user.getRegistrationDate());
            response.setFirstName(user.getFirstName());
            response.setCity(user.getCity());
            response.setBirthDate(user.getBirthDate());
            response.setGender(user.getGender());

            // Set counts for own profile
            int pinsCount = pinRepository.findByUserOrderByCreatedAtDesc(user).size();
            response.setPinsCount(pinsCount);
            int postsCount = postRepository.findByUserOrderByCreatedAtDesc(user).size();
            response.setPostsCount(postsCount);

            response.setFollowersCount(followRepository.countByFollowing(user));
            response.setFollowingCount(followRepository.countByFollower(user));

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

    @GetMapping({"/liked-pins", "/liked-pictures"})
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
            List<PinResponse> pinResponses = likes.stream().map(like -> {
                Pin pin = like.getPin();
                PinResponse dto = pinService.convertToPinResponse(pin, user);
                Picture picture = pin.getPictures();
                if (picture != null) {
                    String thumb1 = picture.getThumbnailImageUrl1();
                    if (thumb1 != null && !thumb1.isEmpty()) {
                        dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
                    }
                }
                List<Location> locs = locationRepository.findByPinId(pin.getId());
                if (!locs.isEmpty()) {
                    Location loc = locs.get(0);
                    dto.setLatitude(loc.getLatitude());
                    dto.setLongitude(loc.getLongitude());
                    dto.setAddress(loc.getAddress());
                    dto.setPlaceName(loc.getNameplace());
                }
                return dto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(pinResponses);
        } catch (Exception e) {
            logger.error("Ошибка при получении лайкнутых пинов", e);
            return ResponseEntity.status(500)
                    .body("Ошибка при получении лайкнутых пинов: " + e.getMessage());
        }
    }

    @GetMapping({"/spots", "/pictures"})
    @Transactional(readOnly = true)
    public ResponseEntity<List<PinResponse>> getOwnProfileSpots() {
        User currentUser = userService.getCurrentUser();
        List<Pin> userPins = pinRepository.findByUserOrderByCreatedAtDesc(currentUser);
        List<PinResponse> pinResponses = userPins.stream().map(pin -> {
            // Базовое преобразование пина в DTO
            PinResponse dto = pinService.convertToPinResponse(pin, currentUser);
            // Применяем первую миниатюру из связанной сущности Picture
            Picture picture = pin.getPictures();
            if (picture != null) {
                String thumb1 = picture.getThumbnailImageUrl1();
                if (thumb1 != null && !thumb1.isEmpty()) {
                    dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
                }
            }
            // Добавляем локацию из таблицы locations
            List<Location> locations = locationRepository.findByPinId(pin.getId());
            if (!locations.isEmpty()) {
                Location loc = locations.get(0);
                dto.setLatitude(loc.getLatitude());
                dto.setLongitude(loc.getLongitude());
                dto.setAddress(loc.getAddress());
                dto.setPlaceName(loc.getNameplace());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(pinResponses);
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
            response.setBio(targetUser.getBio());
            response.setProfileImageUrl(targetUser.getProfileImageUrl() != null ? targetUser.getProfileImageUrl() : "");
            response.setRegistrationDate(targetUser.getRegistrationDate());
            response.setFirstName(targetUser.getFirstName());
            response.setCity(targetUser.getCity());
            response.setBirthDate(targetUser.getBirthDate());
            response.setGender(targetUser.getGender());

            // Set counts for other profile
            int otherPinsCount = pinRepository.findByUserOrderByCreatedAtDesc(targetUser).size();
            response.setPinsCount(otherPinsCount);
            int otherPostsCount = postRepository.findByUserOrderByCreatedAtDesc(targetUser).size();
            response.setPostsCount(otherPostsCount);

            response.setFollowersCount(followRepository.countByFollowing(targetUser));
            response.setFollowingCount(followRepository.countByFollower(targetUser));

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

    @GetMapping("/{userId}/liked-pins")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getLikedPinsByUser(@PathVariable Long userId) {
        try {
            User targetUser = userService.getUserWithCollections(userId);
            List<Like> likes = likeRepository.findByUserOrderByIdDesc(targetUser);
            List<PinResponse> pinResponses = likes.stream().map(like -> {
                Pin pin = like.getPin();
                PinResponse dto = pinService.convertToPinResponse(pin, targetUser);
                Picture picture = pin.getPictures();
                if (picture != null) {
                    String thumb1 = picture.getThumbnailImageUrl1();
                    if (thumb1 != null && !thumb1.isEmpty()) {
                        dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
                    }
                }
                return dto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(pinResponses);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении лайкнутых пинов: " + e.getMessage());
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

    @GetMapping("/posts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostResponse>> getOwnProfilePosts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = userService.getCurrentUser();
        List<Post> userPosts = postRepository.findByUserOrderByCreatedAtDesc(currentUser);
        List<PostResponse> postResponses = userPosts.stream()
                .map(post -> postService.convertToPostResponse(post, currentUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(postResponses);
    }

    @GetMapping("/{userId}/pictures")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PinResponse>> getUserPictures(@PathVariable Long userId) {
        User targetUser = userService.getUserWithCollections(userId);
        List<Pin> userPins = pinRepository.findByUserOrderByCreatedAtDesc(targetUser);
        List<PinResponse> pinResponses = userPins.stream().map(pin -> {
            PinResponse dto = pinService.convertToPinResponse(pin, targetUser);
            Picture picture = pin.getPictures();
            if (picture != null) {
                String thumb1 = picture.getThumbnailImageUrl1();
                if (thumb1 != null && !thumb1.isEmpty()) {
                    dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
                }
            }
            List<Location> locs = locationRepository.findByPinId(pin.getId());
            if (!locs.isEmpty()) {
                Location loc = locs.get(0);
                dto.setLatitude(loc.getLatitude());
                dto.setLongitude(loc.getLongitude());
                dto.setAddress(loc.getAddress());
                dto.setPlaceName(loc.getNameplace());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(pinResponses);
    }

    @GetMapping("/{userId}/posts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long userId) {
        User targetUser = userService.getUserWithCollections(userId);
        List<Post> userPosts = postRepository.findByUserOrderByCreatedAtDesc(targetUser);
        List<PostResponse> postResponses = userPosts.stream()
                .map(post -> postService.convertToPostResponse(post, targetUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(postResponses);
    }

    @GetMapping("/likesPictures/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PinResponse>> getUserLikedPictures(@PathVariable Long userId) {
        User targetUser = userService.getUserWithCollections(userId);
        List<Like> likes = likeRepository.findByUserOrderByIdDesc(targetUser);
        List<PinResponse> pinResponses = likes.stream().map(like -> {
            Pin pin = like.getPin();
            PinResponse dto = pinService.convertToPinResponse(pin, null);
            Picture picture = pin.getPictures();
            if (picture != null) {
                String thumb1 = picture.getThumbnailImageUrl1();
                if (thumb1 != null && !thumb1.isEmpty()) {
                    dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
                }
            }
            List<Location> locs = locationRepository.findByPinId(pin.getId());
            if (!locs.isEmpty()) {
                Location loc = locs.get(0);
                dto.setLatitude(loc.getLatitude());
                dto.setLongitude(loc.getLongitude());
                dto.setAddress(loc.getAddress());
                dto.setPlaceName(loc.getNameplace());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(pinResponses);
    }

    @GetMapping("/{userId}/spots")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PinResponse>> getUserProfileSpots(@PathVariable Long userId) {
        User targetUser = userService.getUserWithCollections(userId);
        List<Pin> userPins = pinRepository.findByUserOrderByCreatedAtDesc(targetUser);
        List<PinResponse> pinResponses = userPins.stream()
                .map(pin -> pinService.convertToPinResponse(pin, targetUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pinResponses);
    }
}