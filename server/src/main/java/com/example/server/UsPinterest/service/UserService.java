package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.RegisterRequest;
import com.example.server.UsPinterest.dto.EditProfileRequest;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.repository.VerificationTokenRepository;
import com.example.server.UsPinterest.security.JwtTokenUtil;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.model.VerificationToken;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.model.Pin;
import org.springframework.context.annotation.Lazy;
import com.example.server.UsPinterest.service.PinCrudService;
import com.example.server.UsPinterest.repository.RefreshTokenRepository;
import com.example.server.UsPinterest.repository.FollowRepository;
import com.example.server.UsPinterest.repository.NotificationRepository;

import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PinRepository pinRepository;
    @Lazy
    private final PinCrudService pinCrudService;
    private final FollowRepository followRepository;
    private final NotificationRepository notificationRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRegistrationDate(LocalDateTime.now());

        // Устанавливаем дополнительные поля
        newUser.setFirstName(request.getFirstName());
        newUser.setBirthDate(request.getBirthDate());

        return userRepository.save(newUser);
    }

    public String loginUser(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        } catch (DisabledException e) {
            logger.warn("Пользователь {} не подтвердил email, выдаем токен: {}", username, e.getMessage());
        }

        // Обновляем время последнего входа
        User user = findByUsername(username).orElseThrow(() -> 
            new RuntimeException("Пользователь не найден")
        );
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtTokenUtil.generateToken(userDetails);
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserWithCollections(Long id) {
        User user = getUserById(id);


        User withBoards = userRepository.findByIdWithBoards(id).orElse(user);
        user.setBoards(withBoards.getBoards());

        User withComments = userRepository.findByIdWithComments(id).orElse(user);
        user.setComments(withComments.getComments());

        User withLikes = userRepository.findByIdWithLikes(id).orElse(user);
        user.setLikes(withLikes.getLikes());

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserWithCollectionsByUsername(String username) {
        User user = findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с именем: " + username));


        User withBoards = userRepository.findByUsernameWithBoards(username).orElse(user);
        user.setBoards(withBoards.getBoards());

        User withComments = userRepository.findByUsernameWithComments(username).orElse(user);
        user.setComments(withComments.getComments());

        User withLikes = userRepository.findByUsernameWithLikes(username).orElse(user);
        user.setLikes(withLikes.getLikes());

        return user;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public User updateProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

        String profileImageUrl = fileStorageService.storeProfileImage(file, userId);
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
            logger.warn("Ошибка при получении текущего пользователя: {}", e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikedPost(Long userId, Long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsernameIgnoreCase(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Transactional
    @CacheEvict(value = {"posts", "pins"}, allEntries = true)
    public void addLikeToPost(User user, Post post) {
        if (!hasUserLikedPost(user.getId(), post.getId())) {
            Like like = new Like();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
        }
    }

    @Transactional
    @CacheEvict(value = {"posts", "pins"}, allEntries = true)
    public void removeLikeFromPost(User user, Post post) {
        likeRepository.deleteByUserIdAndPostId(user.getId(), post.getId());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "'likes_' + #postId")
    public int getLikesCountForPost(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public User editProfile(Long userId, EditProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.example.server.UsPinterest.exception.ResourceNotFoundException("Пользователь не найден с id: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getEmail() != null) {
            // Проверяем, занят ли email другим пользователем
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email уже используется другим пользователем");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        User updated = userRepository.save(user);
        return updated;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + userId));
        followRepository.deleteByFollowerId(userId);
        followRepository.deleteByFollowingId(userId);
        notificationRepository.deleteByRecipient(user);
        notificationRepository.deleteBySender(user);

        refreshTokenRepository.deleteByUser(user);

        List<VerificationToken> tokens = verificationTokenRepository.findByUser(user);
        if (tokens != null && !tokens.isEmpty()) {
            verificationTokenRepository.deleteAll(tokens);
        }

        List<Pin> pins = pinRepository.findByUserId(userId);
        for (Pin p : pins) {
            pinCrudService.deletePin(p.getId());
        }

        userRepository.delete(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Старый пароль неверен");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String createVerificationToken(User user) {
        String token = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusDays(1);
        VerificationToken verificationToken = new VerificationToken(token, user, expiry);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public void confirmEmail(String token) {
        VerificationToken vToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Неверный токен подтверждения"));
        if (vToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Срок действия токена истек");
        }
        User user = vToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(vToken);
    }
}