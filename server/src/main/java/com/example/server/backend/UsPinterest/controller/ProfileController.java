package com.example.diplomwork.backend.UsPinterest.controller;

import com.UsPinterest.dto.PinResponse;
import com.UsPinterest.dto.ProfileResponse;
import com.UsPinterest.exception.ResourceNotFoundException;
import com.UsPinterest.model.Pin;
import com.UsPinterest.model.User;
import com.UsPinterest.repository.PinRepository;
import com.UsPinterest.repository.UserRepository;
import com.UsPinterest.service.BoardService;
import com.UsPinterest.service.PinService;
import com.UsPinterest.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final BoardService boardService;
    private final PinService pinService;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        List<Pin> pins = pinRepository.findByUser(user);

        List<PinResponse> pinResponses = pins.stream().map(pin -> {
            PinResponse pr = new PinResponse();
            pr.setId(pin.getId());
            pr.setImageUrl(pin.getImageUrl());
            pr.setDescription(pin.getDescription());
            return pr;
        }).collect(Collectors.toList());


        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setUsername(user.getUsername());
        profileResponse.setEmail(user.getEmail());
        profileResponse.setPins(pinResponses);

        return ResponseEntity.ok(profileResponse);
    }
} 