package com.example.server.UsPinterest.dto.mapper;

import com.example.server.UsPinterest.dto.BoardRequest;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.server.UsPinterest.service.YandexDiskService;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Маппер для преобразования между сущностью Board и DTO
 */
@Component
public class BoardMapper {

    @Autowired
    private PinMapper pinMapper;

    @Autowired
    private YandexDiskService yandexDiskService;

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

            // Обновляем ссылку на изображение профиля, получая прямую ссылку если возможно
            String profileImageUrl = board.getUser().getProfileImageUrl();
            if (profileImageUrl != null && (profileImageUrl.contains("yadi.sk") || profileImageUrl.contains("disk.yandex.ru"))) {
                try {
                    String directUrl = yandexDiskService.updateImageUrl(profileImageUrl);
                    response.setUserProfileImageUrl(directUrl);
                } catch (Exception e) {
                    // В случае ошибки используем оригинальный URL
                    response.setUserProfileImageUrl(profileImageUrl);
                }
            } else {
                response.setUserProfileImageUrl(profileImageUrl);
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
}