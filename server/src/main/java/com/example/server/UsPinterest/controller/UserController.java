package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
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
}