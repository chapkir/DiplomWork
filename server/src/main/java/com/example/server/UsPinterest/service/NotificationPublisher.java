package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.config.RabbitMQConfig;
import com.example.server.UsPinterest.dto.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final Logger logger = LoggerFactory.getLogger(NotificationPublisher.class);

    public void publishLikeNotification(Long senderId, Long pinId) {
        NotificationEvent event = new NotificationEvent(NotificationEvent.Type.LIKE, senderId, pinId, null);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception ex) {
            logger.warn("Ошибка отправки LikeNotification в RabbitMQ: {}", ex.getMessage());
        }
    }

    public void publishCommentNotification(Long senderId, Long pinId, String commentText) {
        NotificationEvent event = new NotificationEvent(NotificationEvent.Type.COMMENT, senderId, pinId, commentText);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception ex) {
            logger.warn("Ошибка отправки CommentNotification в RabbitMQ: {}", ex.getMessage());
        }
    }
}