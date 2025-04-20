package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @EntityGraph(attributePaths = {"pins"})
    List<Board> findByUserId(Long userId);
} 