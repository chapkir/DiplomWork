package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    boolean existsByTitle(String title);
    Optional<Board> findByTitle(String title);
    @EntityGraph(attributePaths = {"pins"})
    List<Board> findByUserId(Long userId);
} 