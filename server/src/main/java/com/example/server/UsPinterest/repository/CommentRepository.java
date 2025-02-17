package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
} 