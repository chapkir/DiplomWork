package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Photo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
} 