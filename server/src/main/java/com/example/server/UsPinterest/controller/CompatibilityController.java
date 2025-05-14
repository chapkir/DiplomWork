package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CompatibilityController {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityController.class);

    private final PinService pinService;
    private final PinRepository pinRepository;
    private final UserService userService;

    @GetMapping("/api/pins/all")
    @Transactional(readOnly = true)
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

    @GetMapping("/api/pins/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PinResponse> redirectToPinJsonById(
            @PathVariable Long id,
            Authentication authentication) {

        logger.info("Предоставление пина {} в формате объекта JSON", id);

        try {
            User currentUser = authentication != null ?
                    userService.getUserWithCollectionsByUsername(authentication.getName()) : null;

            // Используем новый метод сервиса для загрузки пина с зависимостями
            Pin pin = pinService.getPinWithLikesAndComments(id);

            PinResponse pinResponse = pinService.convertToPinResponse(pin, currentUser);
            return ResponseEntity.ok(pinResponse);

        } catch (ResourceNotFoundException e) {
            logger.warn("Пин с id {} не найден", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Ошибка при получении пина {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}