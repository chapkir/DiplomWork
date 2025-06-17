package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.config.RabbitMQConfig;
import com.example.server.UsPinterest.dto.NotificationEvent;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbit.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationConsumer {

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    private final PinRepository pinRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        Long senderId = event.getSenderId();
        Long pinId = event.getPinId();

        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<Pin> pinOpt = pinRepository.findById(pinId);

        if (senderOpt.isPresent() && pinOpt.isPresent()) {
            User sender = senderOpt.get();
            Pin pin = pinOpt.get();

            switch (event.getType()) {
                case LIKE:
                    notificationService.createLikeNotification(sender, pin);
                    break;
                case COMMENT:
                    notificationService.createCommentNotification(sender, pin, event.getCommentText());
                    break;
            }
        }
    }
}