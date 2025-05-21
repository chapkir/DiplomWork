package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByUser(User user);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.pin.id = :pinId")
    long countByPinId(@Param("pinId") Long pinId);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);
    Page<Comment> findByPin(com.example.server.UsPinterest.model.Pin pin, Pageable pageable);
    
    // Методы для админской панели
    int countByCreatedAtAfter(LocalDateTime date);
    int countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<com.example.server.UsPinterest.entity.Comment> findByPinOrderByIdDesc(com.example.server.UsPinterest.model.Pin pin, Pageable pageable);
    List<com.example.server.UsPinterest.entity.Comment> findByPinAndIdLessThanOrderByIdDesc(com.example.server.UsPinterest.model.Pin pin, Long id, Pageable pageable);
}