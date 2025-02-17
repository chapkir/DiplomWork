package com.example.diplomwork.backend.UsPinterest.repository;

import com.UsPinterest.model.Board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByUserId(Long userId);
} 