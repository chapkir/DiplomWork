package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.service.PinQueryService;
import com.example.server.UsPinterest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final PinRepository pinRepository;
    private final PinQueryService pinQueryService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<PinResponse>> getPinsByCategory(
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(0, size, Sort.by("likesCount").descending());
        List<Pin> pins = pinRepository.findByTags_NameIgnoreCase(categoryName, pageable);
        List<PinResponse> dtos = pins.stream()
                .map(pin -> pinQueryService.enrichPinResponse(
                        pinQueryService.convertToPinResponse(pin, currentUser)
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
} 