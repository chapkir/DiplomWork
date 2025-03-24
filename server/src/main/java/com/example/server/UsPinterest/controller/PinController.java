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
import com.example.server.UsPinterest.service.YandexDiskService;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

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
    private YandexDiskService yandexDiskService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private Bucket bucket;

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
                // Получаем постоянную публичную ссылку на файл
                String imageUrl = yandexDiskService.uploadFile(file);
                logger.info("File uploaded successfully to Yandex.Disk, permanent public URL: {}", imageUrl);

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

    @GetMapping("/update-yandex-links")
    public ResponseEntity<?> updateAllYandexLinks() {
        try {
            int updatedCount = yandexDiskService.updateAllYandexDiskLinks(pinRepository);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Обновлено " + updatedCount + " ссылок на изображения на прямые ссылки Яндекс.Диска");
            response.put("updatedCount", updatedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении ссылок на изображения: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Ошибка при обновлении ссылок: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Исправляет закодированные амперсанды &amp; в URL
     */
    private String fixAmpersands(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        // Заменяем HTML-кодированные амперсанды на настоящие
        return url.replace("&amp;", "&");
    }

    @GetMapping("/force-update-yandex-links")
    public ResponseEntity<?> forceUpdateAllYandexLinks() {
        try {
            logger.info("Запущено принудительное обновление всех ссылок на прямые ссылки Яндекс.Диска");
            List<Pin> pins = pinRepository.findAll();
            int totalCount = pins.size();
            int updatedCount = 0;
            int errorCount = 0;
            int skippedCount = 0;

            for (Pin pin : pins) {
                try {
                    String originalUrl = pin.getImageUrl();
                    if (originalUrl == null || originalUrl.isEmpty()) {
                        logger.debug("Пустая ссылка на изображение для пина {}, пропускаем", pin.getId());
                        skippedCount++;
                        continue;
                    }

                    // Исправляем закодированные амперсанды в URL
                    originalUrl = fixAmpersands(originalUrl);

                    // Если это уже прямая ссылка Яндекс.Диска, проверяем её
                    if (originalUrl.contains("downloader.disk.yandex.ru") ||
                            originalUrl.contains("preview.disk.yandex.ru")) {

                        try {
                            // Проверяем, не устарела ли ссылка
                            URL url = new URL(originalUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("HEAD");
                            connection.setConnectTimeout(5000);
                            int responseCode = connection.getResponseCode();

                            if (responseCode >= 200 && responseCode < 400) {
                                logger.debug("Прямая ссылка для пина {} все еще действительна: {}", pin.getId(), originalUrl);
                                skippedCount++;
                                continue;
                            }

                            logger.info("Прямая ссылка для пина {} устарела (код {}), обновляем", pin.getId(), responseCode);
                        } catch (Exception e) {
                            logger.warn("Ошибка при проверке прямой ссылки {}: {}", originalUrl, e.getMessage());
                        }
                    }

                    // Извлекаем оригинальную ссылку Яндекс.Диска, если это прокси-ссылка
                    String yandexUrl = originalUrl;
                    if (originalUrl.contains("/api/pins/proxy-image") && originalUrl.contains("url=")) {
                        try {
                            String encodedOriginalUrl = originalUrl.split("url=")[1];
                            if (encodedOriginalUrl.contains("&")) {
                                encodedOriginalUrl = encodedOriginalUrl.split("&")[0];
                            }
                            yandexUrl = java.net.URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8.toString());
                            logger.info("Извлечена оригинальная ссылка из прокси: {} -> {}", originalUrl, yandexUrl);
                        } catch (Exception e) {
                            logger.error("Ошибка при извлечении оригинальной ссылки из прокси {}: {}", originalUrl, e.getMessage());
                        }
                    }

                    // Пытаемся получить прямую ссылку
                    boolean isYandexUrl = yandexUrl.contains("yadi.sk") ||
                            yandexUrl.contains("disk.yandex.ru/");

                    String directUrl = null;
                    if (isYandexUrl) {
                        try {
                            directUrl = yandexDiskService.getDownloadLink(yandexUrl);

                            // Проверяем, что это действительно прямая ссылка
                            if (directUrl != null &&
                                    (directUrl.contains("downloader.disk.yandex.ru") ||
                                            directUrl.contains("preview.disk.yandex.ru"))) {

                                pin.setImageUrl(directUrl);
                                pinRepository.save(pin);
                                logger.info("Обновлена ссылка для пина {}: {} -> {}", pin.getId(), originalUrl, directUrl);
                                updatedCount++;
                            } else {
                                logger.warn("Не удалось получить прямую ссылку для пина {}: {}", pin.getId(), yandexUrl);

                                // Если это не прокси-ссылка, сохраняем хотя бы оригинальную ссылку на Яндекс.Диск
                                if (!originalUrl.equals(yandexUrl) &&
                                        (yandexUrl.contains("yadi.sk") || yandexUrl.contains("disk.yandex.ru/"))) {
                                    pin.setImageUrl(yandexUrl);
                                    pinRepository.save(pin);
                                    logger.info("Сохранена оригинальная ссылка Яндекс.Диска вместо прокси: {} -> {}",
                                            originalUrl, yandexUrl);
                                    updatedCount++;
                                } else {
                                    skippedCount++;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Ошибка при получении прямой ссылки для пина {}: {}", pin.getId(), e.getMessage());
                            errorCount++;
                        }
                    } else {
                        logger.debug("URL для пина {} не является ссылкой на Яндекс.Диск: {}", pin.getId(), yandexUrl);
                        skippedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка при обработке пина {}: {}", pin.getId(), e.getMessage());
                    errorCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Принудительное обновление завершено: обновлено " + updatedCount + ", пропущено " +
                    skippedCount + ", ошибок " + errorCount + " из " + totalCount + " ссылок");
            response.put("totalPins", totalCount);
            response.put("updatedCount", updatedCount);
            response.put("skippedCount", skippedCount);
            response.put("errorCount", errorCount);

            logger.info("Принудительное обновление ссылок завершено. Всего: {}, Обновлено: {}, Пропущено: {}, Ошибок: {}",
                    totalCount, updatedCount, skippedCount, errorCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Критическая ошибка при принудительном обновлении ссылок: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Ошибка при обновлении ссылок: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Прокси-метод для загрузки изображений с Яндекс Диска
     * Решает проблему с CORS при загрузке изображений напрямую с Яндекс Диска
     */
    @GetMapping("/proxy-image")
    public ResponseEntity<?> proxyImage(@RequestParam("url") String imageUrl, HttpServletResponse response) {
        try {
            logger.debug("Получен запрос на проксирование изображения: {}", imageUrl);

            // Исправляем закодированные амперсанды в URL
            imageUrl = fixAmpersands(imageUrl);

            // Проверяем, нужно ли добавить параметр disposition=inline для Яндекс.Диска
            if (imageUrl.contains("disk.yandex.ru") && !imageUrl.contains("disposition=")) {
                if (imageUrl.contains("?")) {
                    imageUrl += "&disposition=inline";
                } else {
                    imageUrl += "?disposition=inline";
                }
                logger.debug("Добавлен параметр disposition=inline: {}", imageUrl);
            }

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Добавляем заголовки для имитации браузера
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            connection.setRequestProperty("Referer", "https://disk.yandex.ru/");

            // Получаем данные
            int statusCode = connection.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                String contentType = connection.getContentType();
                if (contentType == null) {
                    contentType = "image/jpeg";  // Предполагаем, что это изображение
                }

                response.setContentType(contentType);

                // Получаем длину контента, если она доступна
                int contentLength = connection.getContentLength();
                if (contentLength > 0) {
                    response.setContentLength(contentLength);
                }

                // Копируем поток данных изображения в ответ
                try (InputStream inputStream = connection.getInputStream();
                     OutputStream outputStream = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                return ResponseEntity.ok().build();
            } else {
                // Если не удалось получить изображение, возвращаем ошибку
                String errorMessage = "Не удалось загрузить изображение. Код ответа: " + statusCode;
                logger.error(errorMessage);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", errorMessage);
                return ResponseEntity.status(statusCode).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("Ошибка при проксировании изображения {}: {}", imageUrl, e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при загрузке изображения: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}