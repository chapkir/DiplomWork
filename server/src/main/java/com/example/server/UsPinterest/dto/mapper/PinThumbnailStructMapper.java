package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.PinThumbnailResponse;
import com.example.server.UsPinterest.model.Pin;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PinThumbnailStructMapper {

    @Mapping(target = "id", source = "pin.id")
    @Mapping(target = "thumbnailImageUrl", source = "pin.thumbnailImageUrl")
    @Mapping(target = "thumbnailWidth", source = "pin.thumbnailWidth")
    @Mapping(target = "thumbnailHeight", source = "pin.thumbnailHeight")
    @Mapping(target = "userId", source = "pin.user.id")
    @Mapping(target = "username", source = "pin.user.username")
    @Mapping(target = "userAvatar", source = "pin.user.profileImageUrl")

    PinThumbnailResponse toDto(Pin pin);
}