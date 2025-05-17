package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.FollowResponse;
import com.example.server.UsPinterest.exception.FollowException;
import com.example.server.UsPinterest.service.FollowService;
import com.example.server.UsPinterest.service.UserService;
import com.example.server.UsPinterest.service.NotificationSender;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Collections;


@RestController
@RequestMapping("/api/follows")
@CrossOrigin(origins = "*")
public class FollowController {

    private final FollowService followService;
    private final UserService userService;
    private final NotificationSender notificationSender;

    public FollowController(FollowService followService, UserService userService, NotificationSender notificationSender) {
        this.followService = followService;
        this.userService = userService;
        this.notificationSender = notificationSender;
    }

    @PostMapping("/{followerId}/following/{followingId}")
    public ResponseEntity<?> follow(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            FollowResponse followResponse = followService.follow(followerId, followingId);
            User follower = userService.getUserById(followerId);
            User following = userService.getUserById(followingId);
            notificationSender.sendNotification(
                following,
                "Новая подписка",
                String.format("%s подписался на вас", follower.getUsername())
            );
            return ResponseEntity.ok(followResponse);
        } catch (FollowException | DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{followerId}/following/{followingId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        followService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<FollowResponse>> getFollowing(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowing(userId, pageable));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<FollowResponse>> getFollowers(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowers(userId, pageable));
    }

    @GetMapping("/{followerId}/following/{followingId}")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        return ResponseEntity.ok(followService.isFollowing(followerId, followingId));
    }

    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowingCount(userId));
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowersCount(userId));
    }
}