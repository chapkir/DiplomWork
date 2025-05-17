package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.FcmTokenRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.FcmTokenService;
import com.example.server.UsPinterest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenService fcmTokenService;
    private final UserService userService;

    @PostMapping("/token")
    public ResponseEntity<?> registerToken(@RequestBody FcmTokenRequest request,
                                           Authentication authentication) {
        User user = userService.getCurrentUser();
        fcmTokenService.registerToken(user, request.getToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token")
    public ResponseEntity<?> removeToken(@RequestBody FcmTokenRequest request,
                                         Authentication authentication) {
        User user = userService.getCurrentUser();
        fcmTokenService.removeToken(user, request.getToken());
        return ResponseEntity.ok().build();
    }
} 