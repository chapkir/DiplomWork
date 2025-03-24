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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Загружаем изображение на Яндекс Диск
        String profileImageUrl = yandexDiskService.uploadProfileImage(file, userId);

        // Обновляем поле profileImageUrl пользователя
        user.setProfileImageUrl(profileImageUrl);

        return userRepository.save(user);
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