package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.CommentResponse;
import com.example.server.UsPinterest.entity.Comment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentStructMapper {

    @Mapping(target = "userProfileImageUrl", source = "comment.user.profileImageUrl")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "mentions", ignore = true)
    CommentResponse toDto(Comment comment);
}