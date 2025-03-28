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
 * REST controller for managing Pins with API versioning
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
     * Get all pins with pagination and cursor-based navigation
     *
     * @param cursor Current cursor for pagination
     * @param size Number of pins per page
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (asc/desc)
     * @return Paginated list of pins with hypermedia links
     */
    @GetMapping
    public ResponseEntity<?> getAllPins(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        // If search is provided, use traditional pagination
        if (search != null && !search.isEmpty()) {
            try {
                logger.info("Searching pins with keyword: {}", search);
                PageResponse<Pin> response = pinService.getPins(search, 0, size);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error searching pins: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error searching pins: " + e.getMessage()));
            }
        }

        // Otherwise use cursor-based pagination
        logger.info("Getting pins with cursor: {}, size: {}, sortBy: {}, sortDirection: {}",
                cursor, size, sortBy, sortDirection);

        try {
            // Получаем текущего пользователя для проверки лайков
            logger.info("Retrieving current user");
            User currentUser = userService.getCurrentUser();
            logger.info("Current user: {}", currentUser != null ? currentUser.getUsername() : "anonymous");

            // Декодируем курсор, если он есть
            Long cursorId = null;
            if (cursor != null && !cursor.isEmpty()) {
                try {
                    logger.info("Decoding cursor: {}", cursor);
                    cursorId = paginationService.decodeCursor(cursor, Long.class);
                    logger.info("Decoded cursor ID: {}", cursorId);
                } catch (Exception e) {
                    logger.error("Error decoding cursor: {}", e.getMessage(), e);
                }
            }

            // Получаем пины с учетом направления сортировки
            List<Pin> pins = new ArrayList<>();
            boolean hasNext = false;
            boolean hasPrevious = cursor != null && !cursor.isEmpty();
            Long nextCursorValue = null;
            Long prevCursorValue = null;

            logger.info("Creating pageable with size: {}", size + 1);
            Pageable pageable = PageRequest.of(0, size + 1);

            if (sortDirection.equalsIgnoreCase("desc")) {
                logger.info("Sorting direction: DESC");
                // Для сортировки по убыванию
                if (cursorId == null) {
                    logger.info("No cursor provided, retrieving all pins");
                    List<Pin> tempPins = pinRepository.findAll(pageable).getContent();
                    pins = new ArrayList<>(tempPins);
                    logger.info("Retrieved {} pins", pins.size());
                } else {
                    logger.info("Finding pins with ID less than {}", cursorId);
                    pins = new ArrayList<>(pinRepository.findByIdLessThanOrderByIdDesc(cursorId, pageable));
                    logger.info("Retrieved {} pins", pins.size());
                }

                if (pins.size() > size) {
                    hasNext = true;
                    pins.remove(pins.size() - 1);
                    nextCursorValue = pins.isEmpty() ? null : pins.get(pins.size() - 1).getId();
                    logger.info("Has next page: true, next cursor value: {}", nextCursorValue);
                } else {
                    logger.info("Has next page: false");
                }

                if (hasPrevious && cursorId != null) {
                    logger.info("Checking for previous page");
                    Pageable prevPageable = PageRequest.of(0, 1);
                    List<Pin> prevPins = pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, prevPageable);
                    prevCursorValue = prevPins.isEmpty() ? null : prevPins.get(0).getId();
                    logger.info("Has previous page: {}, prev cursor value: {}", !prevPins.isEmpty(), prevCursorValue);
                }
            } else {
                logger.info("Sorting direction: ASC");
                // Для сортировки по возрастанию
                if (cursorId == null) {
                    logger.info("No cursor provided, retrieving all pins");
                    List<Pin> tempPins = pinRepository.findAll(pageable).getContent();
                    pins = new ArrayList<>(tempPins);
                    logger.info("Retrieved {} pins", pins.size());
                } else {
                    logger.info("Finding pins with ID greater than {}", cursorId);
                    pins = new ArrayList<>(pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, pageable));
                    logger.info("Retrieved {} pins", pins.size());
                }

                if (pins.size() > size) {
                    hasNext = true;
                    pins.remove(pins.size() - 1);
                    nextCursorValue = pins.isEmpty() ? null : pins.get(pins.size() - 1).getId();
                    logger.info("Has next page: true, next cursor value: {}", nextCursorValue);
                } else {
                    logger.info("Has next page: false");
                }

                if (hasPrevious && cursorId != null) {
                    logger.info("Checking for previous page");
                    Pageable prevPageable = PageRequest.of(0, 1);
                    List<Pin> prevPins = pinRepository.findByIdLessThanOrderByIdDesc(cursorId, prevPageable);
                    prevCursorValue = prevPins.isEmpty() ? null : prevPins.get(0).getId();
                    logger.info("Has previous page: {}, prev cursor value: {}", !prevPins.isEmpty(), prevCursorValue);
                }
            }

            // Преобразуем список пинов в список PinResponse с учетом текущего пользователя
            logger.info("Converting {} pins to PinResponse", pins.size());
            List<PinResponse> pinResponses = new ArrayList<>();
            for (Pin pin : pins) {
                try {
                    PinResponse response = pinService.convertToPinResponse(pin, currentUser);
                    pinResponses.add(response);
                } catch (Exception e) {
                    logger.error("Error converting pin with ID {}: {}", pin.getId(), e.getMessage(), e);
                }
            }
            logger.info("Converted {} pins to responses", pinResponses.size());

            // Создаем ответ с курсорной пагинацией
            logger.info("Creating cursor page response");
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
            logger.info("Adding HATEOAS links");
            HateoasResponse<CursorPageResponse<PinResponse, String>> response = new HateoasResponse<>(pageResponse);
            response.addSelfLink("/api/pins?cursor=" + cursor + "&size=" + size);

            if (hasNext && nextCursorValue != null) {
                String nextCursor = paginationService.encodeCursor(nextCursorValue);
                response.addLink("next", "/api/pins?cursor=" + nextCursor + "&size=" + size, "GET");
                logger.info("Added next link with cursor: {}", nextCursor);
            }

            if (hasPrevious && prevCursorValue != null) {
                String prevCursor = paginationService.encodeCursor(prevCursorValue);
                response.addLink("prev", "/api/pins?cursor=" + prevCursor + "&size=" + size, "GET");
                logger.info("Added prev link with cursor: {}", prevCursor);
            }

            response.addLink("create", "/api/pins", "POST");

            logger.info("Successfully built response");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting pins: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving pins: " + (e.getMessage() != null ? e.getMessage() : "Unknown error, check server logs")));
        }
    }

    /**
     * Get all pins without pagination
     *
     * @return All pins with hypermedia links
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPinsWithoutPagination(Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Getting all pins without pagination");

        try {
            // Получаем текущего пользователя для проверки лайков
            User currentUser = userService.getCurrentUser();
            logger.info("Current user: {}", currentUser != null ? currentUser.getUsername() : "anonymous");

            // Получаем все пины
            List<Pin> pins = pinRepository.findAll();
            logger.info("Retrieved {} pins", pins.size());

            // Преобразуем список пинов в список PinResponse с учетом текущего пользователя
            List<PinResponse> pinResponses = new ArrayList<>();
            for (Pin pin : pins) {
                try {
                    PinResponse response = pinService.convertToPinResponse(pin, currentUser);
                    pinResponses.add(response);
                } catch (Exception e) {
                    logger.error("Error converting pin with ID {}: {}", pin.getId(), e.getMessage(), e);
                }
            }
            logger.info("Converted {} pins to responses", pinResponses.size());

            // Добавляем HATEOAS ссылки
            HateoasResponse<List<PinResponse>> response = new HateoasResponse<>(pinResponses);
            response.addSelfLink("/api/pins/all");
            response.addLink("paginated", "/api/pins", "GET");
            response.addLink("create", "/api/pins", "POST");

            logger.info("Successfully built response");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all pins: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving pins: " + (e.getMessage() != null ? e.getMessage() : "Unknown error, check server logs")));
        }
    }

    /**
     * Get pin by ID with HATEOAS links
     *
     * @param id Pin ID
     * @return Pin with hypermedia links
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPinById(@PathVariable Long id, Authentication authentication) {
        logger.info("Getting pin by id: {}", id);

        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found with id: " + id));

        String username = authentication != null ? authentication.getName() : null;
        User currentUser = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        PinResponse pinResponse = pinService.convertToPinResponse(pin, currentUser);
        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

        // Add HATEOAS links
        response.addSelfLink("/api/pins/" + id);
        response.addLink("all-pins", "/api/pins", "GET");
        response.addUpdateLink("/api/pins/" + id);
        response.addDeleteLink("/api/pins/" + id);

        // Add related resources links
        response.addLink("comments", "/api/pins/" + id + "/comments", "GET");
        response.addLink("likes", "/api/pins/" + id + "/likes", "GET");

        return ResponseEntity.ok(response);
    }

    /**
     * Create a new pin
     *
     * @param pinRequest Pin creation request
     * @return Created pin with hypermedia links
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPin(@Valid @RequestBody PinRequest pinRequest, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Creating pin: {}", pinRequest);

        String username = authentication.getName();
        Pin pin = pinService.createPin(pinRequest, username);
        PinResponse pinResponse = pinService.convertToPinResponse(pin,
                userRepository.findByUsername(username).orElse(null));

        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

        // Add HATEOAS links
        response.addSelfLink("/api/pins/" + pin.getId());
        response.addLink("all-pins", "/api/pins", "GET");
        response.addUpdateLink("/api/pins/" + pin.getId());
        response.addDeleteLink("/api/pins/" + pin.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Like a pin
     *
     * @param id Pin ID
     * @return Success message with metadata
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePin(@PathVariable Long id, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Liking pin with id: {}", id);

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
            response.addLink("pin", "/api/pins/" + id, "GET");
            response.addLink("unlike", "/api/pins/" + id + "/unlike", "POST");
            response.getMeta().setMessage("Pin liked successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing like for pin: {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Unlike a pin
     *
     * @param id Pin ID
     * @return Success message with metadata
     */
    @PostMapping("/{id}/unlike")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unlikePin(@PathVariable Long id, Authentication authentication) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Unliking pin with id: {}", id);

        try {
            Map<String, Object> unlikeResult = pinService.unlikePin(id, authentication.getName());

            HateoasResponse<Void> response = new HateoasResponse<>(null);
            response.addSelfLink("/api/pins/" + id + "/unlike");
            response.addLink("pin", "/api/pins/" + id, "GET");
            response.addLink("like", "/api/pins/" + id + "/like", "POST");
            response.getMeta().setMessage("Pin unliked successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing unlike for pin: {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Add a comment to a pin
     *
     * @param id Pin ID
     * @param commentRequest Comment content
     * @param authentication Current user
     * @return Success message with metadata
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
                    .body(new MessageResponse("Too many requests"));
        }

        logger.info("Adding comment to pin with id: {}", id);
        logger.info("Comment text: {}", commentRequest.getText());

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
            response.addLink("pin", "/api/pins/" + id, "GET");
            response.addLink("all-comments", "/api/pins/" + id + "/comments", "GET");
            response.getMeta().setMessage("Comment added successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding comment to pin: {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Ошибка при добавлении комментария: " + e.getMessage()));
        }
    }

    /**
     * Get comments for a pin
     *
     * @param id Pin ID
     * @return List of comments
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getPinComments(@PathVariable Long id) {
        logger.info("Getting comments for pin with id: {}", id);

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
        response.addLink("pin", "/api/pins/" + id, "GET");
        response.addLink("add-comment", "/api/pins/" + id + "/comments", "POST");

        return ResponseEntity.ok(response);
    }

    /**
     * Upload image for a pin
     *
     * @param file Image file
     * @param description Pin description
     * @param authentication Current user authentication
     * @return Updated pin with hypermedia links
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
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

        logger.info("Uploading image for pin with description: {}", description);

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

            // Add HATEOAS links
            response.addSelfLink("/api/pins/" + pin.getId());
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
     * Upload image for an existing pin
     *
     * @param id Pin ID
     * @param file Image file
     * @return Updated pin with hypermedia links
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadPinImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        logger.info("Uploading image for pin with id: {}", id);

        try {
            // Получаем пин
            Pin pin = pinRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pin not found with id: " + id));

            // Сохраняем файл
            String imageUrl = fileStorageService.storeFile(file);

            // Обновляем URL изображения для пина
            pin.setImageUrl(imageUrl);
            pinRepository.save(pin);

            PinResponse pinResponse = pinService.convertToPinResponse(pin, pin.getUser());

            HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);

            // Add HATEOAS links
            response.addSelfLink("/api/pins/" + id);
            response.addLink("image", pin.getImageUrl(), "GET");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error uploading image for pin: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error uploading image: " + e.getMessage()));
        }
    }

    /**
     * Delete a pin by ID
     *
     * @param id Pin ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deletePin(@PathVariable Long id) {
        logger.info("Deleting pin with id: {}", id);

        pinService.deletePin(id);

        return ResponseEntity.noContent().build();
    }
}