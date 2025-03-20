package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.BoardRequest;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.example.server.UsPinterest.dto.mapper.BoardMapper;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.BoardRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для работы с досками пользователей
 */
@Service
public class BoardService {
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardMapper boardMapper;

    /**
     * Создает новую доску для текущего пользователя
     *
     * @param boardRequest данные для создания доски
     * @return информация о созданной доске
     */
    @Transactional
    @CacheEvict(value = "boards", allEntries = true)
    public BoardResponse createBoard(BoardRequest boardRequest) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь должен быть авторизован");
        }

        Board board = boardMapper.toEntity(boardRequest, currentUser);
        board = boardRepository.save(board);

        logger.info("Создана новая доска: {}, пользователь: {}", board.getTitle(), currentUser.getUsername());

        return boardMapper.toDto(board, currentUser, false);
    }

    /**
     * Получает информацию о доске по ID
     *
     * @param id ID доски
     * @param includePins включать ли пины в ответ
     * @return информация о доске
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "boards", key = "#id + '_' + #includePins")
    public BoardResponse getBoardResponseById(Long id, boolean includePins) {
        User currentUser = userService.getCurrentUser();
        Board board = getBoardById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Доска не найдена с id: " + id));

        return boardMapper.toDto(board, currentUser, includePins);
    }

    /**
     * Получает доску по ID
     *
     * @param id ID доски
     * @return Optional с найденной доской или пустой
     */
    @Transactional(readOnly = true)
    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }

    /**
     * Получает список досок пользователя
     *
     * @param userId ID пользователя
     * @param includePins включать ли пины в ответ
     * @return список DTO досок
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "boards", key = "'user_' + #userId + '_' + #includePins")
    public List<BoardResponse> getBoardsByUserId(Long userId, boolean includePins) {
        User currentUser = userService.getCurrentUser();
        List<Board> boards = boardRepository.findByUserId(userId);

        return boards.stream()
                .map(board -> boardMapper.toDto(board, currentUser, includePins))
                .collect(Collectors.toList());
    }

    /**
     * Удаляет доску по ID (только если пользователь является владельцем)
     *
     * @param id ID доски для удаления
     */
    @Transactional
    @CacheEvict(value = "boards", allEntries = true)
    public void deleteBoard(Long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь должен быть авторизован");
        }

        Board board = getBoardById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Доска не найдена с id: " + id));

        if (!board.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Вы не можете удалить чужую доску");
        }

        boardRepository.deleteById(id);
        logger.info("Удалена доска: {}, пользователь: {}", board.getTitle(), currentUser.getUsername());
    }

    /**
     * Обновляет существующую доску
     *
     * @param id ID доски
     * @param boardRequest данные для обновления
     * @return обновленная информация о доске
     */
    @Transactional
    @CacheEvict(value = "boards", allEntries = true)
    public BoardResponse updateBoard(Long id, BoardRequest boardRequest) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь должен быть авторизован");
        }

        Board board = getBoardById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Доска не найдена с id: " + id));

        if (!board.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Вы не можете обновить чужую доску");
        }

        board = boardMapper.updateEntity(board, boardRequest);
        board = boardRepository.save(board);

        logger.info("Обновлена доска: {}, пользователь: {}", board.getTitle(), currentUser.getUsername());

        return boardMapper.toDto(board, currentUser, false);
    }
} 