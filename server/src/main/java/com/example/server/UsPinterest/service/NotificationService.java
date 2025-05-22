package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.NotificationResponse;
import com.example.server.UsPinterest.model.Notification;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.NotificationRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.repository.FollowRepository;
import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.service.SseService;
import com.example.server.UsPinterest.repository.PictureRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    private final PinRepository pinRepository;

    private final PictureRepository pictureRepository;

    private final FollowRepository followRepository;

    private final UserService userService;

    private final FileStorageService fileStorageService;

    private final SimpMessagingTemplate messagingTemplate;

    private final SseService sseService;

    // Создать уведомление о подписке
    public void createFollowNotification(User sender, User recipient) {
        if (sender.getId().equals(recipient.getId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.FOLLOW);
        notification.setMessage("подписался(-ась) на вас.");
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);
        sendWebSocketNotification(savedNotification);
    }

    // Создать уведомления о новом посте для подписчиков
    public void createPostNotification(User sender, Post post) {
        List<Follow> follows = followRepository.findByFollowing(sender);
        for (Follow follow : follows) {
            User follower = follow.getFollower();
            if (sender.getId().equals(follower.getId())) {
                continue;
            }
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.POST);
            notification.setMessage("опубликовал(-а) пост.");
            notification.setRecipient(follower);
            notification.setSender(sender);
            notification.setPost(post);
            notification.setCreatedAt(LocalDateTime.now());
            Notification savedNotification = notificationRepository.save(notification);
            sendWebSocketNotification(savedNotification);
        }
    }

    // Создать уведомление о лайке
    public void createLikeNotification(User sender, Pin pin) {
        if (pin.getUser().getId().equals(sender.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.LIKE);
        notification.setMessage("нравится ваша картинка.");
        notification.setRecipient(pin.getUser());
        notification.setSender(sender);
        notification.setPin(pin);
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);
        sendWebSocketNotification(savedNotification);
    }

    // Создать уведомление о комментарии
    public void createCommentNotification(User sender, Pin pin, String commentText) {
        if (pin.getUser().getId().equals(sender.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.COMMENT);
        notification.setMessage("оставил(а) комментарий к вашему пину: " +
                (commentText.length() > 50 ? commentText.substring(0, 50) + "..." : commentText));
        notification.setRecipient(pin.getUser());
        notification.setSender(sender);
        notification.setPin(pin);
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);
        sendWebSocketNotification(savedNotification);
    }

    // Создать уведомление о лайке поста
    public void createPostLikeNotification(User sender, Post post) {
        if (post.getUser().getId().equals(sender.getId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.LIKE);
        notification.setMessage("нравится ваш пост.");
        notification.setRecipient(post.getUser());
        notification.setSender(sender);
        notification.setPost(post);
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);
        sendWebSocketNotification(savedNotification);
    }

    // Создать уведомление о комментарии к посту
    public void createPostCommentNotification(User sender, Post post, String commentText) {
        if (post.getUser().getId().equals(sender.getId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.COMMENT);
        String snippet = commentText.length() > 50 ? commentText.substring(0, 50) + "..." : commentText;
        notification.setMessage("прокомментировал(а) ваш пост: " + snippet);
        notification.setRecipient(post.getUser());
        notification.setSender(sender);
        notification.setPost(post);
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);
        sendWebSocketNotification(savedNotification);
    }

    // Создать уведомление об упоминании в комментарии
    public void createMentionNotification(User sender, com.example.server.UsPinterest.entity.Comment comment, User recipient) {
        if (recipient.getId().equals(sender.getId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.MENTION);
        String text = comment.getText();
        String snippet = text.length() > 50 ? text.substring(0, 50) + "..." : text;
        notification.setMessage("отметил(а) вас в комментарии: " + snippet);
        notification.setRecipient(recipient);
        notification.setSender(sender);
        if (comment.getPin() != null) {
            notification.setPin(comment.getPin());
        } else if (comment.getPost() != null) {
            notification.setPost(comment.getPost());
        }
        notification.setCreatedAt(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);
        sendWebSocketNotification(savedNotification);
    }

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

    public Long getUnreadNotificationsCount() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return 0L;
        }

        return notificationRepository.countUnreadNotificationsByUser(currentUser);
    }

    @Transactional
    public void markAllNotificationsAsRead() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        notificationRepository.markAllAsRead(currentUser);
    }

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

    @Transactional
    public void deleteNotificationsByPin(Pin pin) {
        if (pin == null) {
            return;
        }

        notificationRepository.deleteByPin(pin);
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType().toString());
        response.setMessage(notification.getMessage());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());

        if (notification.getSender() != null) {
            response.setSenderId(notification.getSender().getId());
            response.setSenderUsername(notification.getSender().getUsername());
            String avatarUrl = notification.getSender().getProfileImageUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                response.setSenderProfileImageUrl(fileStorageService.updateImageUrl(avatarUrl));
            }
        }

        if (notification.getPin() != null) {
            response.setPinId(notification.getPin().getId());

            // Use compressed WebP variant for pin image in notification
            String imageUrl = null;
            if (notification.getPin().getThumbnailImageUrl() != null && !notification.getPin().getThumbnailImageUrl().isEmpty()) {
                imageUrl = notification.getPin().getThumbnailImageUrl();
            } else if (notification.getPin().getFullhdImageUrl() != null && !notification.getPin().getFullhdImageUrl().isEmpty()) {
                imageUrl = notification.getPin().getFullhdImageUrl();
            } else {
                imageUrl = notification.getPin().getImageUrl();
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                response.setPinImageUrl(fileStorageService.updateImageUrl(imageUrl));
            }

            // Override using Picture entity thumbnail if available
            pictureRepository.findByPinId(notification.getPin().getId()).ifPresent(picture -> {
                if (picture.getThumbnailImageUrl() != null && !picture.getThumbnailImageUrl().isEmpty()) {
                    response.setPinImageUrl(fileStorageService.updateImageUrl(picture.getThumbnailImageUrl()));
                }
            });
        }

        if (notification.getPost() != null) {
            response.setPostId(notification.getPost().getId());
            String postImage = notification.getPost().getImageUrl();
            if (postImage != null && !postImage.isEmpty()) {
                response.setPostImageUrl(fileStorageService.updateImageUrl(postImage));
            }
        }

        return response;
    }

    // Отправка уведомления через WebSocket
    private void sendWebSocketNotification(Notification notification) {
        NotificationResponse response = convertToNotificationResponse(notification);
        messagingTemplate.convertAndSendToUser(
                notification.getRecipient().getUsername(),
                "/queue/notifications",
                response);
        sseService.sendEvent(notification.getRecipient().getId(), response);
    }
}