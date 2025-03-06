package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.YandexDiskService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/pins")
public class PinController {

    private static final Logger logger = LoggerFactory.getLogger(PinController.class);

    @Autowired
    private PinService pinService;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private YandexDiskService yandexDiskService;

    @GetMapping
    public ResponseEntity<List<Pin>> getAllPins() {
        List<Pin> pins = pinRepository.findAll();
        return ResponseEntity.ok(pins);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PinResponse>> getAllPinResponses(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        List<Pin> pins = pinRepository.findAll();
        List<PinResponse> responses = pins.stream().map(pin -> {
            PinResponse pr = new PinResponse();
            pr.setId(pin.getId());

            // Обновляем URL изображения, чтобы получить прямую ссылку на скачивание
            String imageUrl = pin.getImageUrl();
            try {
                imageUrl = yandexDiskService.updateImageUrl(imageUrl);
                // Если URL изменился, обновляем его в базе данных
                if (!imageUrl.equals(pin.getImageUrl())) {
                    pin.setImageUrl(imageUrl);
                    pinRepository.save(pin);
                }
            } catch (Exception e) {
                logger.error("Failed to update image URL for pin {}: {}", pin.getId(), e.getMessage());
            }

            pr.setImageUrl(imageUrl);
            pr.setDescription(pin.getDescription());
            pr.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);
            pr.setIsLikedByCurrentUser(currentUser != null && pin.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId())));

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
        }).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Pin> createPin(@RequestBody PinRequest pinRequest, Authentication authentication) {
        String username = authentication.getName();
        Pin createdPin = pinService.createPin(pinRequest, username);
        return ResponseEntity.ok(createdPin);
    }

    @PostMapping("/{pinId}/likes")
    public ResponseEntity<?> likePin(@PathVariable Long pinId, Authentication authentication) {
        System.out.println("Received like request for pin: " + pinId + " from user: " + authentication.getName());

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(pinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            Optional<Like> likeOptional = likeRepository.findByPinAndUser(pin, user);
            Map<String, Object> responseMap = new HashMap<>();
            if (likeOptional.isPresent()) {
                likeRepository.delete(likeOptional.get());
                responseMap.put("message", "Лайк удалён");
                responseMap.put("liked", false);
                System.out.println("Like removed for pin: " + pinId + " by user: " + user.getUsername());
                return ResponseEntity.ok(responseMap);
            } else {
                Like like = new Like();
                like.setUser(user);
                like.setPin(pin);
                like.setCreatedAt(LocalDateTime.now());
                likeRepository.save(like);
                responseMap.put("message", "Лайк поставлен");
                responseMap.put("liked", true);
                System.out.println("Like added for pin: " + pinId + " by user: " + user.getUsername());
                return ResponseEntity.ok(responseMap);
            }
        } catch (Exception e) {
            System.err.println("Error processing like for pin: " + pinId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{pinId}/comments")
    public ResponseEntity<MessageResponse> addComment(@PathVariable Long pinId,
                                                      @RequestBody CommentRequest commentRequest,
                                                      Authentication authentication) {
        System.out.println("Received comment request for pin: " + pinId + " from user: " + authentication.getName());
        System.out.println("Comment text: " + commentRequest.getText());

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(pinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            Comment comment = new Comment();
            comment.setText(commentRequest.getText());
            comment.setPin(pin);
            comment.setUser(user);
            comment.setCreatedAt(LocalDateTime.now());
            Comment savedComment = commentRepository.save(comment);

            System.out.println("Comment added successfully with ID: " + savedComment.getId());
            return ResponseEntity.ok(new MessageResponse("Комментарий добавлен"));
        } catch (Exception e) {
            System.err.println("Error adding comment for pin: " + pinId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{pinId}")
    public ResponseEntity<PinResponse> getPinById(@PathVariable Long pinId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        PinResponse response = new PinResponse();
        response.setId(pin.getId());

        // Обновляем URL изображения, чтобы получить прямую ссылку на скачивание
        String imageUrl = pin.getImageUrl();
        try {
            imageUrl = yandexDiskService.updateImageUrl(imageUrl);
            // Если URL изменился, обновляем его в базе данных
            if (!imageUrl.equals(pin.getImageUrl())) {
                pin.setImageUrl(imageUrl);
                pinRepository.save(pin);
            }
        } catch (Exception e) {
            logger.error("Failed to update image URL for pin {}: {}", pin.getId(), e.getMessage());
        }

        response.setImageUrl(imageUrl);
        response.setDescription(pin.getDescription());
        response.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);
        response.setIsLikedByCurrentUser(currentUser != null && pin.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(currentUser.getId())));
        response.setComments(
                pin.getComments().stream().map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                    return cr;
                }).collect(Collectors.toList())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pinId}/comments")
    public ResponseEntity<List<CommentResponse>> getPinComments(@PathVariable Long pinId) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        List<CommentResponse> comments = pin.getComments().stream()
                .map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                    return cr;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("description") String description,
                                         Authentication authentication) {
        logger.info("Received upload request for file: {}, description: {}, user: {}",
                file.getOriginalFilename(), description,
                authentication != null ? authentication.getName() : "anonymous");

        try {
            if (authentication == null) {
                logger.error("Authentication is null, access denied");
                return ResponseEntity.status(401).body(new MessageResponse("Unauthorized: Please log in"));
            }

            String username = authentication.getName();
            logger.info("Authenticated user: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found: {}", username);
                        return new RuntimeException("User not found");
                    });

            logger.info("User found: {}, ID: {}", user.getUsername(), user.getId());

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            logger.info("Generated filename: {}", filename);

            try {
                String imageUrl = yandexDiskService.uploadFile(file, filename);
                logger.info("File uploaded successfully to Yandex.Disk, URL: {}", imageUrl);

                Pin pin = new Pin();
                pin.setImageUrl(imageUrl);
                pin.setDescription(description);
                pin.setUser(user);
                pin.setCreatedAt(LocalDateTime.now());

                Pin savedPin = pinRepository.save(pin);
                logger.info("Pin saved to database with ID: {}", savedPin.getId());

                return ResponseEntity.ok(new PinResponse() {{
                    setId(savedPin.getId());
                    setImageUrl(savedPin.getImageUrl());
                    setDescription(savedPin.getDescription());
                }});
            } catch (IOException e) {
                logger.error("Failed to upload image to Yandex.Disk: {}", e.getMessage(), e);
                return ResponseEntity.status(500)
                        .body(new MessageResponse("Failed to upload image: " + e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Server error: " + e.getMessage()));
        }
    }

    @GetMapping("/update-image-urls")
    public ResponseEntity<?> updateAllImageUrls() {
        logger.info("Starting update of all image URLs");

        List<Pin> pins = pinRepository.findAll();
        int updatedCount = 0;
        int failedCount = 0;

        for (Pin pin : pins) {
            try {
                String originalUrl = pin.getImageUrl();
                if (originalUrl != null && !originalUrl.isEmpty()) {
                    String updatedUrl = yandexDiskService.updateImageUrl(originalUrl);

                    if (!originalUrl.equals(updatedUrl)) {
                        pin.setImageUrl(updatedUrl);
                        pinRepository.save(pin);
                        updatedCount++;
                        logger.info("Updated image URL for pin {}: {} -> {}", pin.getId(), originalUrl, updatedUrl);
                    } else {
                        logger.debug("No update needed for pin {}, URL: {}", pin.getId(), originalUrl);
                    }
                }
            } catch (Exception e) {
                failedCount++;
                logger.error("Failed to update image URL for pin {}: {}", pin.getId(), e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalPins", pins.size());
        result.put("updatedPins", updatedCount);
        result.put("failedPins", failedCount);

        logger.info("Finished updating image URLs. Total: {}, Updated: {}, Failed: {}",
                pins.size(), updatedCount, failedCount);

        return ResponseEntity.ok(result);
    }
}