package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.repository.BoardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }

    public List<Board> getBoardsByUserId(Long userId) {
        return boardRepository.findByUserId(userId);
    }

    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }
    

} 