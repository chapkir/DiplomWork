package com.example.diplomwork.backend.UsPinterest.repository;

import com.UsPinterest.entity.Like;
import com.UsPinterest.model.Photo;
import com.UsPinterest.model.Pin;
import com.UsPinterest.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPhotoAndUser(Photo photo, User user);

    Optional<Like> findByPinAndUser(Pin pin, User user);
} 