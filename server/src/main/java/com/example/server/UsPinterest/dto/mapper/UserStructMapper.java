package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserStructMapper {
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    @Mapping(target = "bio", source = "user.bio")
    @Mapping(target = "registrationDate", source = "user.registrationDate")
    @Mapping(target = "pins", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "boards", ignore = true)
    ProfileResponse toProfileDto(User user);
}