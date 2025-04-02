package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.BoardRequest;
import com.example.server.UsPinterest.dto.BoardResponse;
import com.example.server.UsPinterest.service.BoardService;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@Validated
@CrossOrigin(origins = "*")
public class BoardController {
    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    private BoardService boardService;

    @Autowired
    private Bucket bucket;

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody BoardRequest boardRequest) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Запрос на создание доски: {}", boardRequest.getTitle());
        BoardResponse createdBoard = boardService.createBoard(boardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBoard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includePins
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Запрос доски с ID: {}, включая пины: {}", id, includePins);
        BoardResponse board = boardService.getBoardResponseById(id, includePins);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BoardResponse>> getBoardsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean includePins
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Запрос досок пользователя с ID: {}, включая пины: {}", userId, includePins);
        List<BoardResponse> boards = boardService.getBoardsByUserId(userId, includePins);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/me")
    public ResponseEntity<List<BoardResponse>> getMyBoards(
            @RequestParam(defaultValue = "false") boolean includePins,
            Authentication authentication
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Запрос досок текущего пользователя, включая пины: {}", includePins);
        List<BoardResponse> boards = boardService.getBoardsByUserId(null, includePins); // null означает "текущий пользователь"
        return ResponseEntity.ok(boards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardRequest boardRequest
    ) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Запрос на обновление доски с ID: {}", id);
        BoardResponse updatedBoard = boardService.updateBoard(id, boardRequest);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        logger.info("Запрос на удаление доски с ID: {}", id);
        boardService.deleteBoard(id);
        return ResponseEntity.ok().build();
    }
} 