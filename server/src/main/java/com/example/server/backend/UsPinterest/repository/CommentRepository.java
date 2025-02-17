package com.example.diplomwork.backend.UsPinterest.repository;

import com.UsPinterest.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
} 