package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.FollowResponse;
import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования сущности Follow в DTO
 */
@Component
public class FollowMapper {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Преобразует сущность Follow в FollowResponse
     *
     * @param follow сущность подписки
     * @return объект FollowResponse
     */
    public FollowResponse toDto(Follow follow) {
        if (follow == null) {
            return null;
        }

        FollowResponse response = new FollowResponse();
        response.setId(follow.getId());
        response.setCreatedAt(follow.getCreatedAt());

        if (follow.getFollower() != null) {
            response.setFollowerId(follow.getFollower().getId());
            response.setFollowerUsername(follow.getFollower().getUsername());

            // Обновляем ссылку на изображение профиля подписчика
            String followerImageUrl = follow.getFollower().getProfileImageUrl();
            if (followerImageUrl != null && !followerImageUrl.isEmpty()) {
                try {
                    String directUrl = fileStorageService.updateImageUrl(followerImageUrl);
                    response.setFollowerProfileImageUrl(directUrl);
                } catch (Exception e) {
                    // В случае ошибки используем оригинальный URL
                    response.setFollowerProfileImageUrl(followerImageUrl);
                }
            } else {
                response.setFollowerProfileImageUrl(followerImageUrl);
            }
        }

        if (follow.getFollowing() != null) {
            response.setFollowingId(follow.getFollowing().getId());
            response.setFollowingUsername(follow.getFollowing().getUsername());

            // Обновляем ссылку на изображение профиля целевого пользователя
            String followingImageUrl = follow.getFollowing().getProfileImageUrl();
            if (followingImageUrl != null && !followingImageUrl.isEmpty()) {
                try {
                    String directUrl = fileStorageService.updateImageUrl(followingImageUrl);
                    response.setFollowingProfileImageUrl(directUrl);
                } catch (Exception e) {
                    // В случае ошибки используем оригинальный URL
                    response.setFollowingProfileImageUrl(followingImageUrl);
                }
            } else {
                response.setFollowingProfileImageUrl(followingImageUrl);
            }
        }

        return response;
    }
}