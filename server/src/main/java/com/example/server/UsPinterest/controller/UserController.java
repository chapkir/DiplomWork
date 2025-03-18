package com.example.server.UsPinterest.controller;//package com.example.diplomwork.backend.UsPinterest.controller;

import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterRequest> register(@RequestBody RegisterRequest request) {
        User savedUser = userService.registerUser(request);

        RegisterRequest responseDto = new RegisterRequest();
        responseDto.setUsername(savedUser.getUsername());
        responseDto.setEmail(savedUser.getEmail());
        return ResponseEntity.ok(responseDto);
    }

    // Эндпоинт ... в будущем добавить нужно
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    // Создаем объект с информацией о пользователе
                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("id", user.getId());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("profileImageUrl", user.getProfileImageUrl());
                    response.put("bio", user.getBio());
                    response.put("registrationDate", user.getRegistrationDate());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(404).body(
                        java.util.Map.of("message", "Пользователь не найден")
                ));
    }
}