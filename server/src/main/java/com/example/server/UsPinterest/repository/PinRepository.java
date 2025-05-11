package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {
    @EntityGraph(attributePaths = {"likes", "comments"})
    List<Pin> findByBoardId(Long boardId);
    @EntityGraph(attributePaths = {"likes", "comments"})
    List<Pin> findByDescriptionContainingIgnoreCase(String keyword);
    Page<Pin> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);
    List<Pin> findByUserUsername(String username);
    List<Pin> findByUser(User user);
    List<Pin> findByUserOrderByCreatedAtDesc(User user);
    List<Pin> findByUserId(Long userId);
    List<Pin> findByRatingBetween(Double minRating, Double maxRating);
    Page<Pin> findByRatingBetween(Double minRating, Double maxRating, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"likes", "comments"})
    Page<Pin> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"likes", "comments"})
    Optional<Pin> findById(Long id);

    @EntityGraph(attributePaths = {"likes", "comments"})
    List<Pin> findAll();

    @EntityGraph(attributePaths = {"likes", "comments"})
    List<Pin> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);

    @EntityGraph(attributePaths = {"likes", "comments"})
    List<Pin> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}