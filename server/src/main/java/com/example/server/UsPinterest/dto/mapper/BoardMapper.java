package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.BoardRequest;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.server.UsPinterest.service.FileStorageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class BoardMapper {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PinMapper pinMapper;


    public BoardResponse toDto(Board board, User currentUser, boolean includePins) {
        if (board == null) {
            return null;
        }

        BoardResponse response = new BoardResponse();
        response.setId(board.getId());
        response.setTitle(board.getTitle());
        response.setDescription(board.getDescription());

        if (board.getUser() != null) {
            response.setUserId(board.getUser().getId());
            response.setUsername(board.getUser().getUsername());


            String profileImageUrl = board.getUser().getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                response.setUserProfileImageUrl(fileStorageService.updateImageUrl(profileImageUrl));
            } else {
                response.setUserProfileImageUrl(null);
            }
        }

        int pinsCount = board.getPins() != null ? board.getPins().size() : 0;
        response.setPinsCount(pinsCount);

        if (includePins && board.getPins() != null) {
            response.setPins(
                    board.getPins().stream()
                            .map(pin -> pinMapper.toDto(pin, currentUser))
                            .toList()
            );
        } else {
            response.setPins(new ArrayList<>());
        }

        return response;
    }

    public Board toEntity(BoardRequest request, User user) {
        if (request == null) {
            return null;
        }

        Board board = new Board();
        board.setTitle(request.getTitle());
        board.setDescription(request.getDescription());
        board.setUser(user);
        board.setPins(Collections.emptyList());

        return board;
    }

    public Board updateEntity(Board board, BoardRequest request) {
        if (board == null || request == null) {
            return board;
        }

        if (request.getTitle() != null) {
            board.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }

        return board;
    }

    public List<BoardResponse> mapBoardsToBoardResponses(List<Board> boards, boolean includePins) {
        if (boards == null) {
            return new ArrayList<>();
        }

        return boards.stream()
                .map(board -> toDto(board, null, includePins))
                .collect(Collectors.toList());
    }
}