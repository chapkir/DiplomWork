package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.LoginRequest;
import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.dto.TokenRefreshRequest;
import com.example.server.UsPinterest.dto.TokenRefreshResponse;
import com.example.server.UsPinterest.exception.TokenRefreshException;
import com.example.server.UsPinterest.model.RefreshToken;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.security.JwtTokenUtil;
import com.example.server.UsPinterest.security.UserPrincipal;
import com.example.server.UsPinterest.service.EmailService;
import com.example.server.UsPinterest.service.RefreshTokenService;
import com.example.server.UsPinterest.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*")
@RestController
@Timed(value = "auth.controller", description = "Metrics for AuthController endpoints")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    private final RefreshTokenService refreshTokenService;

    private final JwtTokenUtil jwtTokenUtil;

    private final Counter authLoginCounter;

    private final Counter authRegisterCounter;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        authRegisterCounter.increment();
        User savedUser = userService.registerUser(registerRequest);
        // Автоматически подтверждаем аккаунт, отключаем подтверждение через email
        String token = userService.createVerificationToken(savedUser);
        userService.confirmEmail(token);
        // Формируем ответ без токена подтверждения
        Map<String, Object> response = new HashMap<>();
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRefreshResponse> login(@Valid @RequestBody LoginRequest request) {
        authLoginCounter.increment();
        String accessToken = userService.loginUser(request.getUsername(), request.getPassword());
        // Логируем сгенерированный JWT для отладки
        logger.info("Выдан JWT токен для пользователя {}: {}", request.getUsername(), accessToken);
        User user = userService.getUserWithCollectionsByUsername(request.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // Добавляем роль пользователя в ответ
        TokenRefreshResponse response = new TokenRefreshResponse(accessToken, refreshToken.getToken(), user.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserPrincipal userPrincipal = new UserPrincipal(user);
                    String token = jwtTokenUtil.generateToken(userPrincipal);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());
                    // Добавляем роль пользователя в ответ
                    return ResponseEntity.ok(new TokenRefreshResponse(token, newRefreshToken.getToken(), user.getRole()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh токен не найден в базе данных!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
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

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        userService.confirmEmail(token);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email успешно подтвержден");
        return ResponseEntity.ok(response);
    }

    // Тестовый эндпоинт для валидации JWT
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String header) {
        String token = header.startsWith("Bearer ") ? header.substring(7) : header;
        try {
            jwtTokenUtil.validateJwtToken(token);
            return ResponseEntity.ok("JWT валиден");
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body("JWT невалиден: " + e.getMessage());
        }
    }
}