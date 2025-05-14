package com.example.server.UsPinterest.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.example.server.UsPinterest.model.Board;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import com.example.server.UsPinterest.dto.BoardRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardStructMapper {

    @Mapping(target = "userId", source = "board.user.id")
    @Mapping(target = "username", source = "board.user.username")
    @Mapping(target = "pinsCount", expression = "java(board.getPins() != null ? board.getPins().size() : 0)")
    @Mapping(target = "pins", ignore = true)
    // Skip mapping the list of pins here; handle in service based on includePins flag
    BoardResponse toDto(Board board);

    // Map BoardRequest to Board entity; user and pins must be set manually
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "pins", ignore = true)
    Board toEntity(BoardRequest boardRequest);

    // Update existing Board entity from request, ignore null properties
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(BoardRequest boardRequest, @MappingTarget Board board);
}