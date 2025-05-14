package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.model.Pin;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PinStructMapper {

    @Mapping(target = "boardId", expression = "java(pin.getBoard() != null ? pin.getBoard().getId() : null)")
    @Mapping(target = "boardTitle", expression = "java(pin.getBoard() != null ? pin.getBoard().getTitle() : null)")
    @Mapping(target = "userId", source = "pin.user.id")
    @Mapping(target = "username", source = "pin.user.username")
    @Mapping(target = "userProfileImageUrl", source = "pin.user.profileImageUrl")
    @Mapping(target = "rating", source = "pin.rating")
    @Mapping(target = "tags", ignore = true)
    PinResponse toDto(Pin pin);

    @Mapping(target = "tags", ignore = true)
    Pin toEntity(PinRequest request);

    // updateEntity can be implemented manually or via default method later
}