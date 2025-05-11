package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.model.Pin;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PinStructMapper {

    @Mapping(target = "boardId", source = "pin.board.id")
    @Mapping(target = "boardTitle", source = "pin.board.title")
    @Mapping(target = "userId", source = "pin.user.id")
    @Mapping(target = "username", source = "pin.user.username")
    @Mapping(target = "userProfileImageUrl", source = "pin.user.profileImageUrl")
    @Mapping(target = "rating", source = "pin.rating")
    PinResponse toDto(Pin pin);

    Pin toEntity(PinRequest request);

}