package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.HateoasResponse;
import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
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
import com.example.server.UsPinterest.service.NotificationService;
import com.example.server.UsPinterest.service.NotificationPublisher;
import com.example.server.UsPinterest.service.PaginationService;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.util.HateoasUtil;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/pins")
public class PinController {

    private static final Logger logger = LoggerFactory.getLogger(PinController.class);

    @Autowired
    private PinService pinService;

    @Autowired
    private PaginationService paginationService;

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
    private NotificationPublisher notificationPublisher;

    @Autowired
    private Bucket bucket;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HateoasUtil hateoasUtil;


    @GetMapping
    public ResponseEntity<?> getAllPins(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        // Rate limiting
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }
        // Получаем результат курсорной пагинации из сервиса
        CursorPageResponse<PinResponse, String> pageResponse = pinService.getPinsCursor(cursor, size, sortDirection);
        // Формируем HATEOAS-ответ
        HateoasResponse<CursorPageResponse<PinResponse, String>> response =
                hateoasUtil.buildCursorPageResponse(pageResponse, cursor, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-all")
    public ResponseEntity<?> getAllPinsWithoutPagination(Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        try {
            User currentUser = userService.getCurrentUser();
            List<Pin> pins = pinRepository.findAllWithLikesAndComments();

            List<PinResponse> pinResponses = pins.stream()
                    .map(pin -> pinService.convertToPinResponse(pin, currentUser))
                    .collect(Collectors.toList());

            HateoasResponse<List<PinResponse>> response = new HateoasResponse<>(pinResponses);
            response.addSelfLink("/api/pins/list-all");
            response.addLink("paginated", "/api/pins", "GET");
            response.addLink("create", "/api/pins", "POST");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка получения всех пинов: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка получения пинов: " + e.getMessage()));
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getPinById(@PathVariable Long id, Authentication authentication) {
        // Используем сервис для получения пина с комментариями и лайками
        Pin pin = pinService.getPinWithLikesAndComments(id);

        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        PinResponse pinResponse = pinService.convertToPinResponse(pin, currentUser);
        HateoasResponse<PinResponse> response = hateoasUtil.buildPinDetailResponse(pinResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPin(@Valid @RequestBody PinRequest pinRequest, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        String username = authentication.getName();
        Pin pin = pinService.createPin(pinRequest, username);
        PinResponse pinResponse = pinService.convertToPinResponse(pin,
                userRepository.findByUsername(username).orElse(null));

        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

        // Добавляем HATEOAS ссылки
        response.addSelfLink("/api/pins/detail/" + pin.getId());
        response.addLink("all-pins", "/api/pins", "GET");
        response.addUpdateLink("/api/pins/detail/" + pin.getId());
        response.addDeleteLink("/api/pins/detail/" + pin.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/likes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePin(@PathVariable Long id, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        try {
            Map<String, Object> likeResult = pinService.likePin(id, authentication.getName());


            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            // Публикуем событие для асинхронной отправки уведомления
            notificationPublisher.publishLikeNotification(user.getId(), pin.getId());

            HateoasResponse<Void> response = new HateoasResponse<>(null);
            response.addSelfLink("/api/pins/" + id + "/like");
            response.addLink("pin", "/api/pins/detail/" + id, "GET");
            response.addLink("unlike", "/api/pins/" + id + "/unlike", "POST");
            response.getMeta().setMessage("Лайк успешно добавлен");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при добавлении лайка для пина {}: {}", id, e.getMessage());
            throw e;
        }
    }


    @DeleteMapping("/{id}/likes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unlikePin(@PathVariable Long id, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        try {
            Map<String, Object> unlikeResult = pinService.unlikePin(id, authentication.getName());

            HateoasResponse<Void> response = new HateoasResponse<>(null);
            response.addSelfLink("/api/pins/" + id + "/unlike");
            response.addLink("pin", "/api/pins/detail/" + id, "GET");
            response.addLink("like", "/api/pins/" + id + "/like", "POST");
            response.getMeta().setMessage("Лайк успешно удален");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при удалении лайка для пина {}: {}", id, e.getMessage());
            throw e;
        }
    }


    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest commentRequest,
            Authentication authentication) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

            // Загружаем пин с комментариями
            Pin pin = pinRepository.findByIdWithComments(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            Comment comment = new Comment();
            comment.setText(commentRequest.getText());
            comment.setPin(pin);
            comment.setUser(user);
            comment.setCreatedAt(LocalDateTime.now());

            // Сохраняем комментарий в БД
            commentRepository.save(comment);
            // Добавляем комментарий в коллекцию пина и обновляем счётчик комментариев
            pin.getComments().add(comment);
            long totalCommentsLong = commentRepository.countByPinId(id);
            int totalComments = Math.toIntExact(totalCommentsLong);
            pin.setCommentsCount(totalComments);
            pinRepository.save(pin);

            // Публикуем событие для асинхронной отправки уведомления
            notificationPublisher.publishCommentNotification(user.getId(), pin.getId(), commentRequest.getText());

            // Возвращаем обновлённый PinResponse с актуальными счётчиками
            PinResponse pinResponse = pinService.convertToPinResponse(pin, user);
            HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);
            response.addSelfLink("/api/pins/" + id + "/comments");
            response.addLink("pin", "/api/pins/detail/" + id, "GET");
            response.addLink("all-comments", "/api/pins/" + id + "/comments", "GET");
            response.getMeta().setMessage("Комментарий успешно добавлен");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при добавлении комментария к пину {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при добавлении комментария: " + e.getMessage()));
        }
    }


    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getPinComments(@PathVariable Long id) {
        // Загружаем пин с комментариями
        Pin pin = pinRepository.findByIdWithComments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        List<CommentResponse> comments = pin.getComments().stream()
                .map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setCreatedAt(comment.getCreatedAt());
                    if (comment.getUser() != null) {
                        cr.setUsername(comment.getUser().getUsername());
                        String userImg = comment.getUser().getProfileImageUrl();
                        if (userImg != null && !userImg.isEmpty()) {
                            userImg = fileStorageService.updateImageUrl(userImg);
                        }
                        cr.setUserProfileImageUrl(userImg);
                        cr.setUserId(comment.getUser().getId());
                    } else {
                        cr.setUsername("Unknown");
                    }
                    return cr;
                }).collect(Collectors.toList());

        HateoasResponse<List<CommentResponse>> response = new HateoasResponse<>(comments);
        response.addSelfLink("/api/pins/" + id + "/comments");
        response.addLink("pin", "/api/pins/detail/" + id, "GET");
        response.addLink("add-comment", "/api/pins/" + id + "/comments", "POST");

        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "text", defaultValue = "") String text,
            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            Authentication authentication) {

        logger.info("uploadImage called: file={}, text={}, title={}, description={}", file.getOriginalFilename(), text, title, description);

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
            String imageUrl = fileStorageService.storeFile(file);

            // Генерация FullHD и миниатюрных изображений
            String filename = fileStorageService.getFilenameFromUrl(imageUrl);
            String baseName = filename != null && filename.contains(".") ? filename.substring(0, filename.lastIndexOf(".")) : filename;
            FileStorageService.ImageInfo fullhdInfo = fileStorageService.storeFullhdFile(file, baseName);
            FileStorageService.ImageInfo thumbnailInfo = fileStorageService.storeThumbnailFile(file, baseName);

            Pin pin = new Pin();
            // Выбираем title: приоритет у параметра title, иначе text, иначе пустая строка
            String pinTitle = (title != null && !title.isEmpty()) ? title : (text != null ? text : "");
            pin.setTitle(pinTitle);
            pin.setImageUrl(imageUrl);
            pin.setDescription(description);
            pin.setUser(user);
            pin.setCreatedAt(LocalDateTime.now());
            // Вычисляем размеры изображения через сервис
            pinService.calculateImageDimensions(pin);
            // Сохраняем новые WebP-версии в полях сущности
            pin.setFullhdImageUrl(fullhdInfo.getUrl());
            pin.setFullhdWidth(fullhdInfo.getWidth());
            pin.setFullhdHeight(fullhdInfo.getHeight());
            pin.setThumbnailImageUrl(thumbnailInfo.getUrl());
            pin.setThumbnailWidth(thumbnailInfo.getWidth());
            pin.setThumbnailHeight(thumbnailInfo.getHeight());

            Pin savedPin = pinRepository.save(pin);
            PinResponse pinResponse = pinService.convertToPinResponse(savedPin, user);
            HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

            // Добавляем HATEOAS ссылки
            response.addSelfLink("/api/pins/detail/" + savedPin.getId());
            response.addLink("image", savedPin.getImageUrl(), "GET");
            response.addLink("all-pins", "/api/pins", "GET");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обработке загрузки изображения: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при загрузке изображения: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePin(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Пользователь не авторизован"));
            }

            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин с ID " + id + " не найден"));

            if (!pin.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("У вас нет прав на удаление этого пина"));
            }

            notificationService.deleteNotificationsByPin(pin);

            pinService.deletePin(id);

            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Ошибка при удалении пина: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при удалении пина: " + e.getMessage()));
        }
    }

    // Однократный эндпоинт для пересчёта размеров изображений у всех существующих пинов
    @PostMapping("/recalc-dimensions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> recalcImageDimensions() {
        pinService.recalcImageDimensionsForAllPins();
        return ResponseEntity.ok(new MessageResponse("Dimensions recalculated for all pins"));
    }

    @PostMapping("/generate-variants")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> generateImageVariants(Authentication authentication) {
        // Генерация WebP-версий для всех существующих пинов
        pinService.generateImageVariantsForAllPins();
        return ResponseEntity.ok(new MessageResponse("Image variants (FullHD и thumbnails) сгенерированы для всех пинов"));
    }
}