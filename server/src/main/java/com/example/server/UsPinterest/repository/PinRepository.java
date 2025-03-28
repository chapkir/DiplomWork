package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {
    List<Pin> findByBoardId(Long boardId);
    List<Pin> findByDescriptionContainingIgnoreCase(String keyword);
    Page<Pin> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);
    List<Pin> findByUserUsername(String username);
    List<Pin> findByUser(User user);
    List<Pin> findByUserId(Long userId);
    List<Pin> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);
    List<Pin> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}