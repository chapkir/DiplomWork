package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Special controller to provide compatibility for Kotlin clients by returning arrays instead of objects
 */
@RestController
public class CompatibilityController {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityController.class);

    @Autowired
    private PinService pinService;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserService userService;

    /**
     * Основной эндпоинт для получения всех пинов в формате массива JSON для Kotlin-клиентов
     * Решает проблему "Expected BEGIN_ARRAY but was BEGIN_OBJECT"
     */
    @GetMapping("/api/pins/all")
    public ResponseEntity<List<PinResponse>> redirectToPinsJsonAll() {
        logger.info("Предоставление списка пинов в формате массива JSON для Kotlin-клиента");

        try {
            User currentUser = userService.getCurrentUser();
            List<PinResponse> pinResponses = pinRepository.findAll().stream()
                    .map(pin -> pinService.convertToPinResponse(pin, currentUser))
                    .collect(Collectors.toList());

            // Возвращаем массив напрямую, без обертки в объект
            return ResponseEntity.ok(pinResponses);

        } catch (Exception e) {
            logger.error("Ошибка при получении пинов", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Эндпоинт для получения одного пина по ID в формате объекта JSON
     * Исправляет ошибку "Expected BEGIN_OBJECT but was BEGIN_ARRAY" при запросе единичного пина
     */
    @GetMapping("/api/pins/{id}")
    public ResponseEntity<PinResponse> redirectToPinJsonById(
            @PathVariable Long id,
            Authentication authentication) {

        logger.info("Предоставление пина {} в формате объекта JSON", id);

        try {
            User currentUser = authentication != null ?
                    userService.findByUsername(authentication.getName()).orElse(null) : null;

            return pinRepository.findById(id)
                    .map(pin -> {
                        PinResponse pinResponse = pinService.convertToPinResponse(pin, currentUser);
                        return ResponseEntity.ok(pinResponse);
                    })
                    .orElseGet(() -> {
                        return ResponseEntity.notFound().build();
                    });

        } catch (Exception e) {
            logger.error("Ошибка при получении пина {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
}