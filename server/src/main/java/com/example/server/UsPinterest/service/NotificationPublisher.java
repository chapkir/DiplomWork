package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.config.RabbitMQConfig;
import com.example.server.UsPinterest.dto.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLikeNotification(Long senderId, Long pinId) {
        NotificationEvent event = new NotificationEvent(NotificationEvent.Type.LIKE, senderId, pinId, null);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }

    public void publishCommentNotification(Long senderId, Long pinId, String commentText) {
        NotificationEvent event = new NotificationEvent(NotificationEvent.Type.COMMENT, senderId, pinId, commentText);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }
}