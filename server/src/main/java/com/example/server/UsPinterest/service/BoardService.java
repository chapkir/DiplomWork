package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.BoardRequest;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.example.server.UsPinterest.dto.mapper.BoardStructMapper;
import com.example.server.UsPinterest.dto.mapper.PinStructMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class BoardService {
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardStructMapper boardStructMapper;

    @Autowired
    private PinStructMapper pinStructMapper;


    @Transactional
    @CacheEvict(value = "boards", allEntries = true)
    public BoardResponse createBoard(BoardRequest boardRequest) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь должен быть авторизован");
        }

        Board board = boardStructMapper.toEntity(boardRequest);
        board.setUser(currentUser);
        board.setPins(new ArrayList<>());
        board = boardRepository.save(board);

        logger.info("Создана новая доска: {}, пользователь: {}", board.getTitle(), currentUser.getUsername());

        BoardResponse dto = boardStructMapper.toDto(board);
        dto.setPins(new ArrayList<>());
        return dto;
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "boards", key = "#id + '_' + #includePins")
    public BoardResponse getBoardResponseById(Long id, boolean includePins) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Доска не найдена"));

        BoardResponse dto = boardStructMapper.toDto(board);
        if (includePins) {
            dto.setPins(board.getPins().stream()
                    .map(pin -> pinStructMapper.toDto(pin))
                    .collect(Collectors.toList()));
        } else {
            dto.setPins(new ArrayList<>());
        }
        return dto;
    }


    @Transactional(readOnly = true)
    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "boards", key = "'user_' + #userId + '_' + #includePins")
    public List<BoardResponse> getBoardsByUserId(Long userId, boolean includePins) {
        List<Board> boards = boardRepository.findByUserId(userId);
        return boards.stream().map(board -> {
            BoardResponse dto = boardStructMapper.toDto(board);
            if (includePins) {
                dto.setPins(board.getPins().stream()
                        .map(pin -> pinStructMapper.toDto(pin))
                        .collect(Collectors.toList()));
            } else {
                dto.setPins(new ArrayList<>());
            }
            return dto;
        }).collect(Collectors.toList());
    }


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

        boardStructMapper.updateEntityFromRequest(boardRequest, board);
        board = boardRepository.save(board);

        logger.info("Обновлена доска: {}, пользователь: {}", board.getTitle(), currentUser.getUsername());

        BoardResponse dto = boardStructMapper.toDto(board);
        dto.setPins(new ArrayList<>());
        return dto;
    }
} 