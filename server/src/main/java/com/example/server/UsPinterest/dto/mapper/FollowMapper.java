package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.FollowResponse;
import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

    @Autowired
    private FileStorageService fileStorageService;

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

            String followerImageUrl = follow.getFollower().getProfileImageUrl();
            if (followerImageUrl != null && !followerImageUrl.isEmpty()) {
                response.setFollowerProfileImageUrl(fileStorageService.updateImageUrl(followerImageUrl));
            }
        }

        if (follow.getFollowing() != null) {
            response.setFollowingId(follow.getFollowing().getId());
            response.setFollowingUsername(follow.getFollowing().getUsername());

            String followingImageUrl = follow.getFollowing().getProfileImageUrl();
            if (followingImageUrl != null && !followingImageUrl.isEmpty()) {
                response.setFollowingProfileImageUrl(fileStorageService.updateImageUrl(followingImageUrl));
            }
        }

        return response;
    }
}