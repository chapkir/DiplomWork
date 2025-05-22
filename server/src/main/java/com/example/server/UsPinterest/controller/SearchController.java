package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.service.SearchService;
import com.example.server.UsPinterest.model.Category;
import com.example.server.UsPinterest.service.CategoryService;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;
    private final Bucket bucket;
    private final CategoryService categoryService;

    @GetMapping("/pins")
    public ResponseEntity<PageResponse<PinResponse>> searchPins(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Поиск пинов по запросу: {}, теги: {}, страница: {}, размер: {}", query, tags, page, size);
        PageResponse<PinResponse> result = searchService.searchPins(query, tags, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponse<ProfileResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Поиск пользователей по запросу: {}, страница: {}, размер: {}", query, page, size);
        PageResponse<ProfileResponse> result = searchService.searchUsers(query, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Общий поиск по запросу: {}, страница: {}, размер: {}", query, page, size);

        PageResponse<PinResponse> pinsResult = searchService.searchPins(query, null, page, size, "createdAt", "desc");
        PageResponse<ProfileResponse> usersResult = searchService.searchUsers(query, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("pins", pinsResult);
        result.put("users", usersResult);
        List<String> matchingCategories = categoryService.getAllCategories().stream()
            .map(Category::getName)
            .filter(cat -> query == null || query.isEmpty() || cat.toLowerCase().contains(query.toLowerCase()))
            .toList();
        result.put("categories", matchingCategories);

        return ResponseEntity.ok(result);
    }
}