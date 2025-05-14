package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.dto.ChangePasswordRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.dto.MessageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterRequest> register(@RequestBody RegisterRequest request) {
        User savedUser = userService.registerUser(request);

        RegisterRequest responseDto = new RegisterRequest();
        responseDto.setUsername(savedUser.getUsername());
        responseDto.setEmail(savedUser.getEmail());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("profileImageUrl", user.getProfileImageUrl());
                    response.put("bio", user.getBio());
                    response.put("registrationDate", user.getRegistrationDate());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(404).body(
                        Map.of("message", "Пользователь не найден")
                ));
    }

    @GetMapping("/exists/{username}")
    public ResponseEntity<Map<String, Boolean>> existsUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsernameIgnoreCase(username);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteAccount() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Пользователь не авторизован"));
        }
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Пользователь не авторизован"));
        }
        try {
            userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Пароль успешно изменен"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}