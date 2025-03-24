package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.NotificationResponse;
import com.example.server.UsPinterest.model.Notification;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.NotificationRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.service.YandexDiskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private YandexDiskService yandexDiskService;

    // Создать уведомление о лайке
    public void createLikeNotification(User sender, Pin pin) {
        if (pin.getUser().getId().equals(sender.getId())) {
            return; // Не уведомляем, если пользователь лайкнул свой пин
        }

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.LIKE);
        notification.setMessage(sender.getUsername() + " поставил(а) лайк вашему пину");
        notification.setRecipient(pin.getUser());
        notification.setSender(sender);
        notification.setPin(pin);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // Создать уведомление о комментарии
    public void createCommentNotification(User sender, Pin pin, String commentText) {
        if (pin.getUser().getId().equals(sender.getId())) {
            return; // Не уведомляем, если пользователь прокомментировал свой пин
        }

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.COMMENT);
        notification.setMessage(sender.getUsername() + " оставил(а) комментарий к вашему пину: " +
                (commentText.length() > 50 ? commentText.substring(0, 50) + "..." : commentText));
        notification.setRecipient(pin.getUser());
        notification.setSender(sender);
        notification.setPin(pin);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // Получить уведомления пользователя
    public List<NotificationResponse> getUserNotifications(int page, int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser, pageable);

        return notifications.getContent().stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
    }

    // Получить все уведомления пользователя
    public List<NotificationResponse> getAllUserNotifications() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser);

        return notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
    }

    // Получить количество непрочитанных уведомлений
    public Long getUnreadNotificationsCount() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return 0L;
        }

        return notificationRepository.countUnreadNotificationsByUser(currentUser);
    }

    // Пометить все уведомления как прочитанные
    @Transactional
    public void markAllNotificationsAsRead() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        notificationRepository.markAllAsRead(currentUser);
    }

    // Пометить одно уведомление как прочитанное
    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Доступ запрещен");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // Преобразовать уведомление в DTO
    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType().name());
        response.setMessage(notification.getMessage());

        if (notification.getSender() != null) {
            response.setSenderId(notification.getSender().getId());
            response.setSenderUsername(notification.getSender().getUsername());
        }

        if (notification.getPin() != null) {
            response.setPinId(notification.getPin().getId());

            // Обновляем ссылку на изображение пина, получая прямую ссылку если возможно
            String imageUrl = notification.getPin().getImageUrl();
            if (imageUrl != null && (imageUrl.contains("yadi.sk") || imageUrl.contains("disk.yandex.ru"))) {
                try {
                    String directUrl = yandexDiskService.updateImageUrl(imageUrl);
                    response.setPinImageUrl(directUrl);
                } catch (Exception e) {
                    // В случае ошибки используем оригинальный URL
                    response.setPinImageUrl(imageUrl);
                }
            } else {
                response.setPinImageUrl(imageUrl);
            }
        }

        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());

        return response;
    }
}