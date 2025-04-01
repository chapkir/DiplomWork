package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.FollowResponse;
import com.example.server.UsPinterest.exception.FollowException;
import com.example.server.UsPinterest.service.FollowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления подписками пользователей
 */
@RestController
@RequestMapping("/api/follows")
@CrossOrigin(origins = "*")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * Создает подписку одного пользователя на другого
     *
     * @param followerId ID пользователя, который подписывается
     * @param followingId ID пользователя, на которого подписываются
     * @return созданная подписка
     */
    @PostMapping("/{followerId}/following/{followingId}")
    public ResponseEntity<FollowResponse> follow(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            FollowResponse followResponse = followService.follow(followerId, followingId);
            return ResponseEntity.ok(followResponse);
        } catch (FollowException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Отменяет подписку одного пользователя на другого
     *
     * @param followerId ID пользователя, который отписывается
     * @param followingId ID пользователя, от которого отписываются
     * @return статус 204 (No Content) при успешном удалении
     */
    @DeleteMapping("/{followerId}/following/{followingId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        followService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получает список подписок пользователя
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации
     * @return страница подписок
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<FollowResponse>> getFollowing(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowing(userId, pageable));
    }

    /**
     * Получает список подписчиков пользователя
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации
     * @return страница подписчиков
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<FollowResponse>> getFollowers(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowers(userId, pageable));
    }

    /**
     * Проверяет, подписан ли один пользователь на другого
     *
     * @param followerId ID пользователя, который подписывается
     * @param followingId ID пользователя, на которого подписываются
     * @return true, если подписка существует
     */
    @GetMapping("/{followerId}/following/{followingId}")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        return ResponseEntity.ok(followService.isFollowing(followerId, followingId));
    }

    /**
     * Получает количество подписок пользователя
     *
     * @param userId ID пользователя
     * @return количество подписок
     */
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowingCount(userId));
    }

    /**
     * Получает количество подписчиков пользователя
     *
     * @param userId ID пользователя
     * @return количество подписчиков
     */
    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowersCount(userId));
    }
}