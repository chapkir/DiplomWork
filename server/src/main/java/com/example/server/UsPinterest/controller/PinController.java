package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.HateoasResponse;
import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.mapper.PinMapper;
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
import com.example.server.UsPinterest.service.PaginationService;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.service.UserService;
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

/**
 * REST контроллер для управления пинами (изображениями)
 */
@RestController
@RequestMapping("/api/pins")
public class PinController {

    private static final Logger logger = LoggerFactory.getLogger(PinController.class);

    @Autowired
    private PinService pinService;

    @Autowired
    private PinMapper pinMapper;

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
    private Bucket bucket;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    /**
     * Получить все пины с пагинацией и курсорной навигацией
     */
    @GetMapping
    public ResponseEntity<?> getAllPins(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // Проверка лимита запросов
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        // Если есть параметр поиска, используем обычную пагинацию
        if (search != null && !search.isEmpty()) {
            try {
                logger.info("Поиск пинов по ключевому слову: {}", search);
                PageResponse<Pin> response = pinService.getPins(search, 0, size);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Ошибка поиска пинов: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Ошибка поиска пинов: " + e.getMessage()));
            }
        }

        // Иначе используем курсорную пагинацию
        try {
            User currentUser = userService.getCurrentUser();

            // Декодируем курсор, если он есть
            Long cursorId = null;
            if (cursor != null && !cursor.isEmpty()) {
                try {
                    cursorId = paginationService.decodeCursor(cursor, Long.class);
                } catch (Exception e) {
                    logger.error("Ошибка декодирования курсора: {}", e.getMessage());
                }
            }

            // Получаем пины с учетом направления сортировки
            List<Pin> pins = new ArrayList<>();
            boolean hasNext = false;
            boolean hasPrevious = cursor != null && !cursor.isEmpty();
            Long nextCursorValue = null;
            Long prevCursorValue = null;

            Pageable pageable = PageRequest.of(0, size + 1);

            if (sortDirection.equalsIgnoreCase("desc")) {
                // Для сортировки по убыванию
                if (cursorId == null) {
                    pins = new ArrayList<>(pinRepository.findAll(pageable).getContent());
                } else {
                    pins = new ArrayList<>(pinRepository.findByIdLessThanOrderByIdDesc(cursorId, pageable));
                }

                if (pins.size() > size) {
                    hasNext = true;
                    pins.remove(pins.size() - 1);
                    nextCursorValue = pins.isEmpty() ? null : pins.get(pins.size() - 1).getId();
                }

                if (hasPrevious && cursorId != null) {
                    Pageable prevPageable = PageRequest.of(0, 1);
                    List<Pin> prevPins = pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, prevPageable);
                    prevCursorValue = prevPins.isEmpty() ? null : prevPins.get(0).getId();
                }
            } else {
                // Для сортировки по возрастанию
                if (cursorId == null) {
                    pins = new ArrayList<>(pinRepository.findAll(pageable).getContent());
                } else {
                    pins = new ArrayList<>(pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, pageable));
                }

                if (pins.size() > size) {
                    hasNext = true;
                    pins.remove(pins.size() - 1);
                    nextCursorValue = pins.isEmpty() ? null : pins.get(pins.size() - 1).getId();
                }

                if (hasPrevious && cursorId != null) {
                    Pageable prevPageable = PageRequest.of(0, 1);
                    List<Pin> prevPins = pinRepository.findByIdLessThanOrderByIdDesc(cursorId, prevPageable);
                    prevCursorValue = prevPins.isEmpty() ? null : prevPins.get(0).getId();
                }
            }

            // Преобразуем список пинов в список PinResponse
            List<PinResponse> pinResponses = pins.stream()
                    .map(pin -> pinService.convertToPinResponse(pin, currentUser))
                    .collect(Collectors.toList());

            // Создаем ответ с курсорной пагинацией
            CursorPageResponse<PinResponse, String> pageResponse = paginationService.createCursorPageResponse(
                    pinResponses,
                    nextCursorValue,
                    prevCursorValue,
                    hasNext,
                    hasPrevious,
                    size,
                    pinRepository.count()
            );

            // Добавляем HATEOAS ссылки
            HateoasResponse<CursorPageResponse<PinResponse, String>> response = new HateoasResponse<>(pageResponse);
            response.addSelfLink("/api/pins?cursor=" + cursor + "&size=" + size);

            if (hasNext && nextCursorValue != null) {
                String nextCursor = paginationService.encodeCursor(nextCursorValue);
                response.addLink("next", "/api/pins?cursor=" + nextCursor + "&size=" + size, "GET");
            }

            if (hasPrevious && prevCursorValue != null) {
                String prevCursor = paginationService.encodeCursor(prevCursorValue);
                response.addLink("prev", "/api/pins?cursor=" + prevCursor + "&size=" + size, "GET");
            }

            response.addLink("create", "/api/pins", "POST");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка получения пинов: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка получения пинов: " + e.getMessage()));
        }
    }

    /**
     * Получить все пины без пагинации
     */
    @GetMapping("/list-all")
    public ResponseEntity<?> getAllPinsWithoutPagination(Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Слишком много запросов"));
        }

        try {
            User currentUser = userService.getCurrentUser();
            List<Pin> pins = pinRepository.findAll();

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

    /**
     * Получить пин по ID
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getPinById(@PathVariable Long id, Authentication authentication) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден с id: " + id));

        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        PinResponse pinResponse = pinService.convertToPinResponse(pin, currentUser);
        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

        // Добавляем HATEOAS ссылки
        response.addSelfLink("/api/pins/detail/" + id);
        response.addLink("all-pins", "/api/pins", "GET");
        response.addUpdateLink("/api/pins/detail/" + id);
        response.addDeleteLink("/api/pins/detail/" + id);
        response.addLink("comments", "/api/pins/detail/" + id + "/comments", "GET");
        response.addLink("likes", "/api/pins/detail/" + id + "/likes", "GET");

        return ResponseEntity.ok(response);
    }

    /**
     * Создать новый пин
     */
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

    /**
     * Поставить лайк пину
     */
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

            // Создаем уведомление о лайке
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            notificationService.createLikeNotification(user, pin);

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

    /**
     * Убрать лайк с пина
     */
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

    /**
     * Добавить комментарий к пину
     */
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
            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            Comment comment = new Comment();
            comment.setText(commentRequest.getText());
            comment.setPin(pin);
            comment.setUser(user);
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);

            // Создаем уведомление о комментарии
            notificationService.createCommentNotification(user, pin, commentRequest.getText());

            HateoasResponse<Void> response = new HateoasResponse<>(null);
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

    /**
     * Получить комментарии к пину
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getPinComments(@PathVariable Long id) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        List<CommentResponse> comments = pin.getComments().stream()
                .map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                    return cr;
                }).collect(Collectors.toList());

        HateoasResponse<List<CommentResponse>> response = new HateoasResponse<>(comments);
        response.addSelfLink("/api/pins/" + id + "/comments");
        response.addLink("pin", "/api/pins/detail/" + id, "GET");
        response.addLink("add-comment", "/api/pins/" + id + "/comments", "POST");

        return ResponseEntity.ok(response);
    }

    /**
     * Загрузить изображение для пина
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            Authentication authentication) {

        // Проверяем лимит запросов
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

            Pin pin = new Pin();
            pin.setImageUrl(imageUrl);
            pin.setDescription(description);
            pin.setUser(user);
            pin.setCreatedAt(LocalDateTime.now());

            pinRepository.save(pin);

            PinResponse pinResponse = pinService.convertToPinResponse(pin, user);
            HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

            // Добавляем HATEOAS ссылки
            response.addSelfLink("/api/pins/detail/" + pin.getId());
            response.addLink("image", pin.getImageUrl(), "GET");
            response.addLink("all-pins", "/api/pins", "GET");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Ошибка при загрузке изображения", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при загрузке изображения: " + e.getMessage()));
        }
    }

    /**
     * Удалить пин по ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePin(@PathVariable Long id, Authentication authentication) {
        try {
            // Получаем текущего пользователя
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Пользователь не авторизован"));
            }

            // Получаем пин
            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин с ID " + id + " не найден"));

            // Проверяем, является ли текущий пользователь владельцем пина
            if (!pin.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("У вас нет прав на удаление этого пина"));
            }

            // Удаляем связанные уведомления перед удалением пина
            notificationService.deleteNotificationsByPin(pin);

            // Удаляем пин
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
}