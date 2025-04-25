package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.model.Post;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByUser(User user);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.pin.id = :pinId")
    long countByPinId(@Param("pinId") Long pinId);
}