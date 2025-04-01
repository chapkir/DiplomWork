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

/**
 * Маппер для преобразования между сущностью Board и DTO
 */
@Component
public class BoardMapper {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PinMapper pinMapper;

    /**
     * Преобразует Board в BoardResponse
     *
     * @param board сущность доски
     * @param currentUser текущий пользователь (для проверки лайков)
     * @param includePins включать ли пины в ответ
     * @return объект BoardResponse
     */
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

            // Обновляем ссылку на изображение профиля
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

    /**
     * Создает сущность Board из BoardRequest
     *
     * @param request запрос на создание доски
     * @param user пользователь, создающий доску
     * @return сущность Board
     */
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

    /**
     * Обновляет существующую сущность Board из BoardRequest
     *
     * @param board существующая доска
     * @param request запрос на обновление
     * @return обновленная сущность Board
     */
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

    /**
     * Convert a list of Board entities to BoardResponse DTOs
     *
     * @param boards The list of Board entities
     * @param includePins Whether to include pin data
     * @return List of BoardResponse DTOs
     */
    public List<BoardResponse> mapBoardsToBoardResponses(List<Board> boards, boolean includePins) {
        if (boards == null) {
            return new ArrayList<>();
        }

        return boards.stream()
                .map(board -> toDto(board, null, includePins))
                .collect(Collectors.toList());
    }
}