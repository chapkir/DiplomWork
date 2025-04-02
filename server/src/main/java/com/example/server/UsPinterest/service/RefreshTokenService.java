package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.exception.TokenRefreshException;
import com.example.server.UsPinterest.model.RefreshToken;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.RefreshTokenRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Optional<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));


        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            logger.info("Удаляем существующий refresh токен для пользователя: {}", user.getUsername());
            refreshTokenRepository.delete(token);
        });

        Instant now = Instant.now();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(now.plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(now);
        refreshToken.setRevoked(false);

        logger.info("Создан новый refresh токен для пользователя: {}", user.getUsername());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            logger.warn("Попытка использовать отозванный токен: {}", token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh токен был отозван. Пожалуйста, авторизуйтесь заново");
        }

        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            logger.warn("Попытка использовать просроченный токен: {}", token.getToken());
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new TokenRefreshException(token.getToken(), "Refresh токен просрочен. Пожалуйста, авторизуйтесь заново");
        }

        return token;
    }


    @Transactional
    public void revokeToken(RefreshToken token) {
        logger.info("Отзываем refresh токен: {}", token.getToken());
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }


    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        logger.info("Удаляем все refresh токены для пользователя: {}", user.getUsername());
        return refreshTokenRepository.deleteByUser(user);
    }
}