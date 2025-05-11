package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.PinFullHdResponse;
import com.example.server.UsPinterest.model.Pin;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PinFullHdStructMapper {

    @Mapping(target = "id", source = "pin.id")
    @Mapping(target = "fullhdImageUrl", source = "pin.fullhdImageUrl")
    @Mapping(target = "fullhdWidth", source = "pin.fullhdWidth")
    @Mapping(target = "fullhdHeight", source = "pin.fullhdHeight")
    @Mapping(target = "userId", source = "pin.user.id")
    @Mapping(target = "username", source = "pin.user.username")
    @Mapping(target = "userAvatar", source = "pin.user.profileImageUrl")

    PinFullHdResponse toDto(Pin pin);
}