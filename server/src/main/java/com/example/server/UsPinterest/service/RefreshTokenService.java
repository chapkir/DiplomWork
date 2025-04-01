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

    /**
     * Находит токен по его значению
     *
     * @param token строковое значение токена
     * @return Optional с найденным токеном или пустой Optional
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Находит токен по пользователю
     *
     * @param user пользователь
     * @return Optional с найденным токеном или пустой Optional
     */
    public Optional<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    /**
     * Создает новый refresh токен для пользователя
     *
     * @param userId ID пользователя
     * @return созданный RefreshToken
     */
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        // Удаляем существующий токен, если есть
        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            logger.info("Удаляем существующий refresh токен для пользователя: {}", user.getUsername());
            refreshTokenRepository.delete(token);
        });

        // Устанавливаем текущее время
        Instant now = Instant.now();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(now.plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(now);
        refreshToken.setRevoked(false); // явно указываем, что токен не отозван

        logger.info("Создан новый refresh токен для пользователя: {}", user.getUsername());

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Проверяет, не истек ли срок действия токена и не отозван ли он
     *
     * @param token токен для проверки
     * @return RefreshToken, если токен действителен
     * @throws TokenRefreshException если токен просрочен или отозван
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        // Проверяем, не отозван ли токен
        if (token.isRevoked()) {
            logger.warn("Попытка использовать отозванный токен: {}", token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh токен был отозван. Пожалуйста, авторизуйтесь заново");
        }

        // Проверяем, не истек ли срок действия токена
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            logger.warn("Попытка использовать просроченный токен: {}", token.getToken());
            // Отмечаем токен как отозванный вместо удаления
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new TokenRefreshException(token.getToken(), "Refresh токен просрочен. Пожалуйста, авторизуйтесь заново");
        }

        return token;
    }

    /**
     * Отзывает токен
     *
     * @param token токен для отзыва
     */
    @Transactional
    public void revokeToken(RefreshToken token) {
        logger.info("Отзываем refresh токен: {}", token.getToken());
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /**
     * Удаляет токен для пользователя
     *
     * @param userId ID пользователя
     * @return количество удаленных токенов
     */
    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        logger.info("Удаляем все refresh токены для пользователя: {}", user.getUsername());
        return refreshTokenRepository.deleteByUser(user);
    }
}