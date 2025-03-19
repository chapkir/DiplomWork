package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.FollowResponse;
import com.example.server.UsPinterest.model.Follow;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования сущности Follow в DTO
 */
@Component
public class FollowMapper {

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
            response.setFollowerProfileImageUrl(follow.getFollower().getProfileImageUrl());
        }

        if (follow.getFollowing() != null) {
            response.setFollowingId(follow.getFollowing().getId());
            response.setFollowingUsername(follow.getFollowing().getUsername());
            response.setFollowingProfileImageUrl(follow.getFollowing().getProfileImageUrl());
        }

        return response;
    }
}