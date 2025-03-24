package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.server.UsPinterest.service.YandexDiskService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью Pin и DTO PinResponse/PinRequest
 */
@Component
public class PinMapper {

    @Autowired
    private YandexDiskService yandexDiskService;

    /**
     * Преобразует сущность Pin в DTO PinResponse с указанием, лайкнул ли текущий пользователь пин
     *
     * @param pin          сущность пина
     * @param currentUser  текущий пользователь (может быть null)
     * @return             объект PinResponse с данными пина
     */
    public PinResponse toDto(Pin pin, User currentUser) {
        if (pin == null) {
            return null;
        }

        PinResponse response = new PinResponse();
        response.setId(pin.getId());

        // Обновляем ссылку на изображение, получая прямую ссылку если возможно
        String imageUrl = pin.getImageUrl();
        if (imageUrl != null && (imageUrl.contains("yadi.sk") || imageUrl.contains("disk.yandex.ru"))) {
            try {
                String directUrl = yandexDiskService.updateImageUrl(imageUrl);
                response.setImageUrl(directUrl);
            } catch (Exception e) {
                // В случае ошибки используем оригинальный URL
                response.setImageUrl(imageUrl);
            }
        } else {
            response.setImageUrl(imageUrl);
        }

        response.setDescription(pin.getDescription());

        // Информация о доске
        if (pin.getBoard() != null) {
            response.setBoardId(pin.getBoard().getId());
            response.setBoardTitle(pin.getBoard().getTitle());
        }

        // Информация о пользователе
        if (pin.getUser() != null) {
            response.setUserId(pin.getUser().getId());
            response.setUsername(pin.getUser().getUsername());
            response.setUserProfileImageUrl(pin.getUser().getProfileImageUrl());
        }

        // Добавление даты создания
        response.setCreatedAt(pin.getCreatedAt());

        // Счетчик лайков
        response.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);

        // Флаг, лайкнул ли текущий пользователь пин
        boolean isLikedByCurrentUser = false;
        if (currentUser != null && pin.getLikes() != null) {
            isLikedByCurrentUser = pin.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        }
        response.setIsLikedByCurrentUser(isLikedByCurrentUser);

        // Добавление комментариев
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

    /**
     * Создает новую сущность Pin из данных PinRequest
     *
     * @param request объект запроса на создание пина
     * @return        сущность Pin (без сохранения в БД)
     */
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

    /**
     * Обновляет существующую сущность Pin данными из PinRequest
     *
     * @param pin     существующая сущность пина
     * @param request объект запроса с новыми данными
     * @return        обновленная сущность Pin
     */
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
}