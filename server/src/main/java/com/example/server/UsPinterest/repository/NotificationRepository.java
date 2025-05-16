package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Notification;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.model.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = ?1 AND n.isRead = false")
    Long countUnreadNotificationsByUser(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = ?1")
    void markAllAsRead(User user);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.pin = ?1")
    void deleteByPin(Pin pin);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.post = ?1")
    void deleteByPost(Post post);

    @Transactional
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.post.id = ?1")
    void deleteByPostId(Long postId);

    @Modifying
    @Transactional
    int deleteByRecipient(User recipient);

    @Modifying
    @Transactional
    int deleteBySender(User sender);
}