package com.example.server.UsPinterest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.annotation.Timed;

import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PinRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Timed(value = "test.controller", description = "Metrics for TestController endpoints")
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private PinService pinService;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        logger.info("Получен запрос к /api/test");

        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "API работает!");
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("Отправка успешного ответа: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Ошибка при обработке запроса: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("timestamp", System.currentTimeMillis());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/test/cache/{id}")
    public ResponseEntity<?> testCache(@PathVariable Long id) {
        logger.info("Тестирование кэша для пина с ID: {}", id);

        try {
            Optional<Pin> firstCall = pinService.getPinById(id);

            Optional<Pin> secondCall = pinService.getPinById(id);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Проверьте логи сервера. Если кэш работает, вы увидите сообщение о загрузке из базы данных только один раз.");
            response.put("pinFound", firstCall.isPresent());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании кэша: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/test/pagination")
    public ResponseEntity<?> testPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search) {

        logger.info("Тестирование пагинации: страница={}, размер={}, поиск={}", page, size, search);

        try {
            PageResponse<Pin> pageResponse = pinService.getPins(search, page, size);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("page", pageResponse.getPageNo());
            response.put("size", pageResponse.getPageSize());
            response.put("totalElements", pageResponse.getTotalElements());
            response.put("totalPages", pageResponse.getTotalPages());
            response.put("isLastPage", pageResponse.isLast());
            response.put("content", pageResponse.getContent());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании пагинации: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/test/cache/clear")
    public ResponseEntity<?> clearCache() {
        logger.info("Очистка всех кэшей");

        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                logger.info("Очистка кэша: {}", cacheName);
                cacheManager.getCache(cacheName).clear();
            });

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Все кэши успешно очищены");
            response.put("clearedCaches", cacheManager.getCacheNames());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при очистке кэша: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ошибка при очистке кэша: " + e.getMessage());
        }
    }

    @GetMapping("/test/fix-database")
    @Transactional
    public ResponseEntity<?> fixDatabaseIntegrity() {
        logger.info("Исправление целостности базы данных");

        try {
            List<Comment> invalidComments = commentRepository.findAll().stream()
                    .filter(comment -> comment.getPin() == null || pinRepository.findById(comment.getPin().getId()).isEmpty())
                    .collect(Collectors.toList());

            List<Like> invalidLikes = likeRepository.findAll().stream()
                    .filter(like -> like.getPin() == null || pinRepository.findById(like.getPin().getId()).isEmpty())
                    .collect(Collectors.toList());

            commentRepository.deleteAll(invalidComments);

            likeRepository.deleteAll(invalidLikes);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "База данных успешно исправлена");
            response.put("removedComments", invalidComments.size());
            response.put("removedLikes", invalidLikes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при исправлении базы данных: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ошибка при исправлении базы данных: " + e.getMessage());
        }
    }
} 