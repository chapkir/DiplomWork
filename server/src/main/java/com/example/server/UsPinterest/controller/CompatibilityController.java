package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.service.PinService;
import com.example.server.UsPinterest.service.UserService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

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


@RestController
public class CompatibilityController {

    private static final Logger logger = LoggerFactory.getLogger(CompatibilityController.class);

    @Autowired
    private PinService pinService;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserService userService;

    private final Bucket bucket;

    public CompatibilityController() {
        // Define rate limit
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, java.time.Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
    }


    @GetMapping("/api/pins/all")
    public ResponseEntity<List<PinResponse>> redirectToPinsJsonAll() {
        logger.info("Предоставление списка пинов в формате массива JSON для Kotlin-клиента");

        try {
            User currentUser = userService.getCurrentUser();
            List<PinResponse> pinResponses = pinRepository.findAll().stream()
                    .map(pin -> pinService.convertToPinResponse(pin, currentUser))
                    .collect(Collectors.toList());


            return ResponseEntity.ok(pinResponses);

        } catch (Exception e) {
            logger.error("Ошибка при получении пинов: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }


    @GetMapping("/api/pins/{id}")
    public ResponseEntity<List<PinResponse>> redirectToPinJsonById(
            @PathVariable Long id,
            Authentication authentication) {

        logger.info("Предоставление пина {} в формате массива JSON для Kotlin-клиента", id);

        try {
            User currentUser = authentication != null ?
                    userService.findByUsername(authentication.getName()).orElse(null) : null;

            return pinRepository.findById(id)
                    .map(pin -> {
                        List<PinResponse> responseList = new ArrayList<>();
                        PinResponse pinResponse = pinService.convertToPinResponse(pin, currentUser);
                        responseList.add(pinResponse);
                        return ResponseEntity.ok(responseList);
                    })
                    .orElseGet(() -> {
                        return ResponseEntity.ok(new ArrayList<>());
                    });

        } catch (Exception e) {
            logger.error("Ошибка при получении пина {}: {}", id, e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}