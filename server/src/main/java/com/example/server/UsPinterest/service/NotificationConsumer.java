package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.config.RabbitMQConfig;
import com.example.server.UsPinterest.dto.NotificationEvent;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationConsumer {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PinRepository pinRepository;

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