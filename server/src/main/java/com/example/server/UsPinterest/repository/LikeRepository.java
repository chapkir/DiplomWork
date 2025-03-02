package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.model.Photo;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPhotoAndUser(Photo photo, User user);

    Optional<Like> findByPinAndUser(Pin pin, User user);

    List<Like> findByUser(User user);
} 