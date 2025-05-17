package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.entity.FcmToken;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByUser(User user);
    void deleteByUserAndToken(User user, String token);
} 