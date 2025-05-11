package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.service.SearchService;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @Autowired
    private Bucket bucket;

    @GetMapping("/pins")
    public ResponseEntity<PageResponse<PinResponse>> searchPins(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Поиск пинов по запросу: {}, страница: {}, размер: {}", query, page, size);
        PageResponse<PinResponse> result = searchService.searchPins(query, page, size, sortBy, sortDirection);
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

        PageResponse<PinResponse> pinsResult = searchService.searchPins(query, page, size, "createdAt", "desc");
        PageResponse<ProfileResponse> usersResult = searchService.searchUsers(query, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("pins", pinsResult);
        result.put("users", usersResult);

        return ResponseEntity.ok(result);
    }
}