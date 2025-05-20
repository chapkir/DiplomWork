package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    @EntityGraph(attributePaths = {"comments"})
    List<Post> findByUserOrderByCreatedAtDesc(User user);
    @EntityGraph(attributePaths = {"comments"})
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Методы для админской панели
    int countByCreatedAtAfter(LocalDateTime date);
    int countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}