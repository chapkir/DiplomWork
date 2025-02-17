package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.LoginRequest;
import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Контроллер для регистрации и авторизации пользователей
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<RegisterRequest> register(@RequestBody RegisterRequest registerRequest) {
        User savedUser = userService.registerUser(registerRequest);

        RegisterRequest responseDto = new RegisterRequest();
        responseDto.setUsername(savedUser.getUsername());
        responseDto.setEmail(savedUser.getEmail());

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.loginUser(request.getUsername(), request.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
} 