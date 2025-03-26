package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
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
import com.example.server.UsPinterest.service.NotificationService;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.FileStorageService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/pins")
@Validated
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
    private NotificationService notificationService;

    @Autowired
    private Bucket bucket;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<?> getAllPins(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        PageResponse<Pin> response = pinService.getPins(search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PinResponse>> getAllPinResponses(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        List<Pin> pins = pinRepository.findAll();
        List<PinResponse> responses = pins.stream()
                .map(pin -> pinService.convertToPinResponse(pin, currentUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Pin> createPin(
            @Valid @RequestBody PinRequest pinRequest,
            Authentication authentication) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(null);
        }

        String username = authentication.getName();
        Pin createdPin = pinService.createPin(pinRequest, username);
        return ResponseEntity.ok(createdPin);
    }

    @PostMapping("/{pinId}/likes")
    public ResponseEntity<?> likePin(
            @PathVariable Long pinId,
            Authentication authentication) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Received like request for pin: {} from user: {}", pinId, authentication.getName());
        try {
            Map<String, Object> response = pinService.likePin(pinId, authentication.getName());

            // Создаем уведомление о лайке
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(pinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            notificationService.createLikeNotification(user, pin);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing like for pin: {}: {}", pinId, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{pinId}/likes")
    public ResponseEntity<?> unlikePin(
            @PathVariable Long pinId,
            Authentication authentication) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Received unlike request for pin: {} from user: {}", pinId, authentication.getName());
        try {
            Map<String, Object> response = pinService.unlikePin(pinId, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing unlike for pin: {}: {}", pinId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{pinId}/comments")
    public ResponseEntity<MessageResponse> addComment(
            @PathVariable Long pinId,
            @Valid @RequestBody CommentRequest commentRequest,
            Authentication authentication) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Received comment request for pin: {} from user: {}", pinId, authentication.getName());
        logger.info("Comment text: {}", commentRequest.getText());

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
            commentRepository.save(comment);

            // Создаем уведомление о комментарии
            notificationService.createCommentNotification(user, pin, commentRequest.getText());

            return ResponseEntity.ok(new MessageResponse("Комментарий успешно добавлен"));
        } catch (Exception e) {
            logger.error("Error adding comment to pin: {}: {}", pinId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при добавлении комментария: " + e.getMessage()));
        }
    }

    @GetMapping("/{pinId}")
    public ResponseEntity<PinResponse> getPinById(@PathVariable Long pinId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        return ResponseEntity.ok(pinService.convertToPinResponse(pin, currentUser));
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
        // Check rate limiting
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов. Пожалуйста, попробуйте позже."));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Файл не выбран"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Файл должен быть изображением"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        try {
            // Use FileStorageService instead of YandexDiskService
            String imageUrl = fileStorageService.storeFile(file);

            Pin pin = new Pin();
            pin.setImageUrl(imageUrl);
            pin.setDescription(description);
            pin.setUser(user);
            pin.setCreatedAt(LocalDateTime.now());

            pinRepository.save(pin);

            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("pinId", pin.getId());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Ошибка при загрузке изображения", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при загрузке изображения: " + e.getMessage()));
        }
    }
}