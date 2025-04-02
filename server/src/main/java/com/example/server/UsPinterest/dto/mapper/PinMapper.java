package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.service.FileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PinMapper {

    @Autowired
    private FileStorageService fileStorageService;

    public PinResponse toDto(Pin pin, User currentUser) {
        if (pin == null) {
            return null;
        }

        PinResponse response = new PinResponse();
        response.setId(pin.getId());

        String imageUrl = pin.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            response.setImageUrl(fileStorageService.updateImageUrl(imageUrl));
        } else {
            response.setImageUrl(imageUrl);
        }

        response.setDescription(pin.getDescription());

        if (pin.getBoard() != null) {
            response.setBoardId(pin.getBoard().getId());
            response.setBoardTitle(pin.getBoard().getTitle());
        }

        if (pin.getUser() != null) {
            response.setUserId(pin.getUser().getId());
            response.setUsername(pin.getUser().getUsername());

            String profileImageUrl = pin.getUser().getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                response.setUserProfileImageUrl(fileStorageService.updateImageUrl(profileImageUrl));
            } else {
                response.setUserProfileImageUrl(profileImageUrl);
            }
        }

        response.setCreatedAt(pin.getCreatedAt());

        response.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);

        boolean isLikedByCurrentUser = false;
        if (currentUser != null && pin.getLikes() != null) {
            isLikedByCurrentUser = pin.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        }
        response.setIsLikedByCurrentUser(isLikedByCurrentUser);

        if (pin.getComments() != null) {
            response.setComments(
                    pin.getComments().stream()
                            .map(comment -> {
                                CommentResponse cr = new CommentResponse();
                                cr.setId(comment.getId());
                                cr.setText(comment.getText());
                                cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                                return cr;
                            })
                            .collect(Collectors.toList())
            );
        } else {
            response.setComments(Collections.emptyList());
        }

        return response;
    }

    public Pin toEntity(PinRequest request) {
        if (request == null) {
            return null;
        }

        Pin pin = new Pin();
        pin.setImageUrl(request.getImageUrl());
        pin.setDescription(request.getDescription());
        pin.setCreatedAt(LocalDateTime.now());

        return pin;
    }

    public Pin updateEntity(Pin pin, PinRequest request) {
        if (pin == null || request == null) {
            return pin;
        }

        if (request.getImageUrl() != null) {
            pin.setImageUrl(request.getImageUrl());
        }

        if (request.getDescription() != null) {
            pin.setDescription(request.getDescription());
        }

        return pin;
    }


    public List<PinResponse> mapPinsToPinResponses(List<Pin> pins, User currentUser) {
        return pins.stream()
                .map(pin -> toDto(pin, currentUser))
                .collect(Collectors.toList());
    }
}