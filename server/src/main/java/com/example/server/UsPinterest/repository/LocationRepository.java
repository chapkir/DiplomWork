package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByPostId(Long postId);
    List<Location> findByPinId(Long pinId);
}