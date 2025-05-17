package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.entity.FcmToken;
import com.example.server.UsPinterest.model.User;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationSender {
    private final FcmTokenService fcmTokenService;

    public void sendNotification(User user, String title, String body) {
        List<String> tokens = fcmTokenService.getTokensByUser(user).stream()
                .map(FcmToken::getToken)
                .collect(Collectors.toList());
        if (tokens.isEmpty()) {
            return;
        }
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            // Можно логировать результат: success/failure counts
        } catch (Exception e) {
            // Логируем ошибку отправки push-уведомления
            // logger.error("Ошибка отправки FCM уведомления: {}", e.getMessage(), e);
        }
    }
} 