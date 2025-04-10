package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PostRequest;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.security.CurrentUser;
import com.example.server.UsPinterest.security.UserPrincipal;
import com.example.server.UsPinterest.service.PostService;
import com.example.server.UsPinterest.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final FileStorageService fileStorageService;

    @Autowired
    public PostController(PostService postService, FileStorageService fileStorageService) {
        this.postService = postService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> createPost(
            @RequestBody PostRequest postRequest,
            @CurrentUser UserPrincipal currentUser) {
        PostResponse postResponse = postService.createPost(postRequest, currentUser.getId());
        return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
    }

    /**
     * Создаёт новый пост с изображением.
     * Требует авторизации с валидным JWT токеном в заголовке Authorization: Bearer {token}
     *
     * @param file Файл изображения
     * @param text Текст поста
     * @param currentUser Текущий авторизованный пользователь (устанавливается автоматически через @CurrentUser)
     * @return Созданный пост или сообщение об ошибке
     */
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Используем оба способа авторизации для защиты
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPostWithImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @CurrentUser UserPrincipal currentUser) {
        try {
            // Проверка на авторизацию
            if (currentUser == null) {
                logger.error("UserPrincipal is null despite @PreAuthorize annotation");
                // Проверяем содержимое контекста безопасности
                logger.error("SecurityContext authentication: {}",
                        SecurityContextHolder.getContext().getAuthentication());
                Map<String, String> response = new HashMap<>();
                response.put("error", "Пользователь не авторизован");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            logger.info("Received request to create post with image. Username: {}, Text length: {}, File: {}, File size: {}",
                    currentUser.getUsername(),
                    text != null ? text.length() : 0,
                    file != null ? file.getOriginalFilename() : "null",
                    file != null ? file.getSize() : 0);

            Long userId = currentUser.getId();
            logger.info("Using authenticated user id: {}", userId);

            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                Map<String, String> response = new HashMap<>();
                response.put("error", "Файл не был выбран или пуст");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            String contentType = file.getContentType();
            logger.info("File content type: {}", contentType);

            if (contentType == null || !contentType.startsWith("image/")) {
                logger.error("Unsupported media type: {}", contentType);
                Map<String, String> response = new HashMap<>();
                response.put("error", "Неподдерживаемый тип файла: " + contentType);
                return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }

            // Проверка, существует ли директория для загрузки файлов
            try {
                fileStorageService.checkAndCreateStorageDirectories();
                logger.info("Storage directories checked/created successfully");
            } catch (Exception e) {
                logger.error("Error checking/creating storage directories", e);
            }

            logger.info("Storing file: {}, size: {}, type: {}", file.getOriginalFilename(), file.getSize(), file.getContentType());
            String imageUrl = fileStorageService.storeFile(file);
            logger.info("File stored successfully. URL: {}", imageUrl);

            PostRequest postRequest = new PostRequest();
            postRequest.setText(text);
            postRequest.setImageUrl(imageUrl);

            logger.info("Creating post with text: {} and image URL: {}", text, imageUrl);
            PostResponse postResponse = postService.createPost(postRequest, userId);
            logger.info("Post created successfully. ID: {}", postResponse.getId());

            return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("IO Exception when storing file", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ошибка при сохранении файла: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception when creating post with image", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ошибка при создании публикации: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@CurrentUser UserPrincipal currentUser) {
        List<PostResponse> posts = postService.getAllPosts(currentUser);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<PostResponse>> getPaginatedPosts(
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {
        Page<PostResponse> posts = postService.getPaginatedPosts(pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        PostResponse post = postService.getPostById(postId, currentUser);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequest postRequest) {
        PostResponse postResponse = postService.updatePost(postId, postRequest);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> likePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        PostResponse postResponse = postService.likePost(postId, currentUser.getId());
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        List<PostResponse> posts = postService.getUserPosts(userId, currentUser);
        return ResponseEntity.ok(posts);
    }

    // Тестовый метод для создания постов, может использоваться без авторизации, но использует пользователя если он авторизован
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(value = "/test-upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> testUploadWithImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("TEST ENDPOINT: Received request to create post with image. Text length: {}, File: {}, File size: {}",
                    text != null ? text.length() : 0,
                    file != null ? file.getOriginalFilename() : "null",
                    file != null ? file.getSize() : 0);

            // Используем ID авторизованного пользователя, если он есть, иначе используем тестовый ID
            Long userId = 1L; // ID тестового пользователя по умолчанию
            if (currentUser != null) {
                userId = currentUser.getId();
                logger.info("Using authenticated user id: {}", userId);
            } else {
                logger.warn("No authenticated user found, using default test user with id: {}", userId);
            }

            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                Map<String, String> response = new HashMap<>();
                response.put("error", "Файл не был выбран или пуст");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            String contentType = file.getContentType();
            logger.info("File content type: {}", contentType);

            if (contentType == null || !contentType.startsWith("image/")) {
                logger.error("Unsupported media type: {}", contentType);
                Map<String, String> response = new HashMap<>();
                response.put("error", "Неподдерживаемый тип файла: " + contentType);
                return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }

            // Проверка, существует ли директория для загрузки файлов
            try {
                fileStorageService.checkAndCreateStorageDirectories();
                logger.info("Storage directories checked/created successfully");
            } catch (Exception e) {
                logger.error("Error checking/creating storage directories", e);
            }

            logger.info("Storing file: {}, size: {}, type: {}", file.getOriginalFilename(), file.getSize(), file.getContentType());
            String imageUrl = fileStorageService.storeFile(file);
            logger.info("File stored successfully. URL: {}", imageUrl);

            PostRequest postRequest = new PostRequest();
            postRequest.setText(text);
            postRequest.setImageUrl(imageUrl);

            logger.info("Creating post with text: {} and image URL: {}", text, imageUrl);
            PostResponse postResponse = postService.createPost(postRequest, userId);
            logger.info("Post created successfully. ID: {}", postResponse.getId());

            return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("IO Exception when storing file", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ошибка при сохранении файла: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception when creating post with image", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ошибка при создании публикации: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Обработчик исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        logger.error("Unhandled exception in PostController", e);
        Map<String, String> response = new HashMap<>();
        response.put("error", "Внутренняя ошибка сервера: " + e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}