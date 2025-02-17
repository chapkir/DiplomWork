package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin, Long> {
    List<Pin> findByBoardId(Long boardId);
    List<Pin> findByDescriptionContainingIgnoreCase(String keyword);
    List<Pin> findByUserUsername(String username);
    List<Pin> findByUser(User user);
}