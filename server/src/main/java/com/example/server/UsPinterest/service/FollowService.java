package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.FollowResponse;
import com.example.server.UsPinterest.dto.mapper.FollowMapper;
import com.example.server.UsPinterest.exception.FollowException;
import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.FollowRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    public FollowService(FollowRepository followRepository, UserRepository userRepository, FollowMapper followMapper) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.followMapper = followMapper;
    }


    @Transactional
    public FollowResponse follow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));

        if (followerId.equals(followingId)) {
            throw new FollowException("Нельзя подписаться на самого себя");
        }

        // Проверяем наличие подписки по id, чтобы обойти возможные проблемы с сущностями
        if (followRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent()) {
            throw new FollowException("Подписка уже существует");
        }

        try {
            // Используем прямой SQL запрос через новый метод репозитория
            LocalDateTime now = LocalDateTime.now();
            followRepository.createFollowManually(followerId, followingId, now);

            // Получаем созданную запись
            Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                    .orElseThrow(() -> new FollowException("Ошибка при создании подписки"));

            return followMapper.toDto(follow);
        } catch (DataIntegrityViolationException e) {
            String causeMessage = e.getMostSpecificCause().getMessage().toLowerCase();
            // если уникальное ограничение по подписке
            if (causeMessage.contains("unique") || causeMessage.contains("duplicate key")) {
                throw new FollowException("Подписка уже существует");
            }
            // прочие нарушения целостности
            throw e;
        }
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));

        long deleted = followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        if (deleted == 0) {
            throw new FollowException("Подписка не найдена");
        }
    }

    public Page<FollowResponse> getFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));

        Page<Follow> follows = followRepository.findByFollower(user, pageable);

        return follows.map(followMapper::toDto);
    }

    public Page<FollowResponse> getFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));

        Page<Follow> follows = followRepository.findByFollowing(user, pageable);

        return follows.map(followMapper::toDto);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));
        // Проверяем наличие подписки по id
        return followRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent();
    }

    public long getFollowingCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));
        return followRepository.countByFollower(user);
    }

    public long getFollowersCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FollowException("Пользователь не найден"));
        return followRepository.countByFollowing(user);
    }
}