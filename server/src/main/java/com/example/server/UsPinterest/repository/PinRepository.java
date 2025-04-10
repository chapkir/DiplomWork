package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {
    List<Pin> findByBoardId(Long boardId);
    List<Pin> findByDescriptionContainingIgnoreCase(String keyword);
    Page<Pin> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);
    List<Pin> findByUserUsername(String username);
    List<Pin> findByUser(User user);
    List<Pin> findByUserOrderByCreatedAtDesc(User user);
    List<Pin> findByUserId(Long userId);
    List<Pin> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);
    List<Pin> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    @Query("SELECT p FROM Pin p LEFT JOIN FETCH p.likes WHERE p.id = :id")
    Optional<Pin> findByIdWithLikesAndComments(@Param("id") Long id);

    @Query("SELECT p FROM Pin p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Optional<Pin> findByIdWithComments(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pin p LEFT JOIN FETCH p.likes")
    List<Pin> findAllWithLikes();

    @Query("SELECT DISTINCT p FROM Pin p LEFT JOIN FETCH p.likes WHERE p.id < :id ORDER BY p.id DESC")
    List<Pin> findByIdLessThanWithLikesOrderByIdDesc(@Param("id") Long id, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pin p LEFT JOIN FETCH p.likes WHERE p.id > :id ORDER BY p.id ASC")
    List<Pin> findByIdGreaterThanWithLikesOrderByIdAsc(@Param("id") Long id, Pageable pageable);
}