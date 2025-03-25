package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.LoginRequest;
import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.dto.TokenRefreshRequest;
import com.example.server.UsPinterest.dto.TokenRefreshResponse;
import com.example.server.UsPinterest.exception.TokenRefreshException;
import com.example.server.UsPinterest.model.RefreshToken;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.security.JwtTokenUtil;
import com.example.server.UsPinterest.service.RefreshTokenService;
import com.example.server.UsPinterest.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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

        // Получаем пользователя
        User user = userService.findByUsername(request.getUsername()).orElseThrow();

        // Создаем refresh токен
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken.getToken());
        response.put("tokenType", "Bearer");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword())
                            .authorities("ROLE_USER")
                            .build();

                    // Генерируем новый token доступа
                    String token = jwtTokenUtil.generateToken(userDetails);

                    // Создаем новый refresh токен
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    // Возвращаем новую пару токенов
                    return ResponseEntity.ok(new TokenRefreshResponse(token, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh токен не найден в базе данных!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            // Найти все refresh токены пользователя и отозвать их
            Optional<RefreshToken> refreshToken = refreshTokenService.findByUser(currentUser);
            refreshToken.ifPresent(token -> refreshTokenService.revokeToken(token));

            Map<String, String> response = new HashMap<>();
            response.put("message", "Вы успешно вышли из системы");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", currentUser.getId());
        response.put("username", currentUser.getUsername());
        response.put("email", currentUser.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", currentUser.getId());
        response.put("username", currentUser.getUsername());
        response.put("email", currentUser.getEmail());
        response.put("profileImageUrl", currentUser.getProfileImageUrl());
        response.put("bio", currentUser.getBio());
        response.put("registrationDate", currentUser.getRegistrationDate());

        return ResponseEntity.ok(response);
    }
}