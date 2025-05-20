package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.FeedbackRequest;
import com.example.server.UsPinterest.service.FeedbackService;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> submitFeedback(@RequestBody FeedbackRequest request) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        feedbackService.createFeedback(currentUser, request);
        return ResponseEntity.ok().build();
    }
} 