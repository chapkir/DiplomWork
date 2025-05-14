package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PostRequest;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.security.CurrentUser;
import com.example.server.UsPinterest.security.UserPrincipal;
import com.example.server.UsPinterest.service.PostService;
import com.example.server.UsPinterest.service.CommentService;
import com.example.server.UsPinterest.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Timed(value = "post.controller", description = "Metrics for PostController endpoints")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final CommentService commentService;
    private final Counter postCreateCounter;
    private final Counter profileImageUploadCounter;
    private final DistributionSummary fileUploadSizeSummary;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest postRequest,
                                                   @CurrentUser UserPrincipal currentUser) {
        postCreateCounter.increment();
        PostResponse postResponse = postService.createPost(postRequest, currentUser.getId());
        return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPostWithImage(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("text") String text,
                                                 @CurrentUser UserPrincipal currentUser) {
        postCreateCounter.increment();
        fileUploadSizeSummary.record(file.getSize());
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

            // Generate FullHD WebP variant for post image
            FileStorageService.ImageInfo fullhdInfo = fileStorageService.storeFullhdFile(file, null);
            String imageUrl = fullhdInfo.getUrl();

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

    @DeleteMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> unlikePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("Removing like from post with id: {}, User: {}", postId, currentUser.getUsername());
        PostResponse postResponse = postService.unlikePost(postId, currentUser.getId());
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
    public ResponseEntity<?> testUploadWithImage(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("text") String text,
                                                 @CurrentUser UserPrincipal currentUser) {
        profileImageUploadCounter.increment();
        fileUploadSizeSummary.record(file.getSize());
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

            // Generate FullHD WebP variant for post image
            FileStorageService.ImageInfo fullhdInfo = fileStorageService.storeFullhdFile(file, null);
            String imageUrl = fullhdInfo.getUrl();

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

    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest commentRequest,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("Adding comment to post with id: {}, User: {}", postId, currentUser.getUsername());
        CommentResponse commentResponse = commentService.addCommentToPost(postId, commentRequest, currentUser.getId());
        return new ResponseEntity<>(commentResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getPostComments(@PathVariable Long postId) {
        logger.info("Getting comments for post with id: {}", postId);
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("Deleting comment with id: {} from post with id: {}, User: {}",
                commentId, postId, currentUser.getUsername());
        commentService.deleteComment(commentId, postId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cursor")
    public ResponseEntity<CursorPageResponse<PostResponse, String>> getPostsCursor(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "12") int size) {
        CursorPageResponse<PostResponse, String> page = postService.getPostsCursor(cursor, size);
        return ResponseEntity.ok(page);
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