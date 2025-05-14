package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    List<Picture> findByPinId(Long pinId);
} 