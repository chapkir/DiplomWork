package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.security.JwtTokenUtil;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private YandexDiskService yandexDiskService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRegistrationDate(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    public String loginUser(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtTokenUtil.generateToken(userDetails);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Обновляет изображение профиля пользователя
     *
     * @param userId идентификатор пользователя
     * @param file файл изображения
     * @return обновленный пользователь
     * @throws IOException если произошла ошибка при загрузке файла
     */
    public User updateProfileImage(Long userId, MultipartFile file) throws IOException {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

            // Проверяем размер файла (максимум 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
            }

            // Проверяем тип файла
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Файл должен быть изображением");
            }

            // Загружаем изображение на Яндекс Диск
            String profileImageUrl = yandexDiskService.uploadProfileImage(file, userId);
            if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                throw new IOException("Не удалось получить URL загруженного изображения");
            }

            // Обновляем поле profileImageUrl пользователя
            user.setProfileImageUrl(profileImageUrl);

            return userRepository.save(user);
        } catch (ResourceNotFoundException e) {
            logger.error("Пользователь не найден при обновлении изображения профиля: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Некорректные параметры при обновлении изображения профиля: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error("Ошибка при загрузке изображения профиля: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при обновлении изображения профиля: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обновить изображение профиля", e);
        }
    }

    public User getCurrentUser() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
                return null;
            }

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return findByUsername(userDetails.getUsername()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
} 