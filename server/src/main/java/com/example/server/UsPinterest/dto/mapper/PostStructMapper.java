package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.PostResponse;
import com.example.server.UsPinterest.dto.PostRequest;
import com.example.server.UsPinterest.model.Post;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostStructMapper {

    @Mapping(target = "userId", source = "post.user.id")
    @Mapping(target = "username", source = "post.user.username")
    @Mapping(target = "userAvatar", source = "post.user.avatarUrl")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likesCount", source = "post.likesCount")
    @Mapping(target = "isLikedByCurrentUser", source = "post.likedByCurrentUser")
    @Mapping(target = "commentsCount", expression = "java(post.getCommentsCount() != null ? post.getCommentsCount() : post.getComments().size())")
    PostResponse toDto(Post post);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Post toEntity(PostRequest request);
}