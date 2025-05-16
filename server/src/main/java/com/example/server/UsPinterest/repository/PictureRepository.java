package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    Optional<Picture> findByPinId(Long pinId);
} 