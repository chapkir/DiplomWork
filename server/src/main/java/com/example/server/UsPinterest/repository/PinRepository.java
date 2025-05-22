package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.model.Location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {
    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    List<Pin> findByBoardId(Long boardId);
    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    List<Pin> findByDescriptionContainingIgnoreCase(String keyword);
    Page<Pin> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);
    List<Pin> findByUserUsername(String username);
    List<Pin> findByUser(User user);
    List<Pin> findByUserOrderByCreatedAtDesc(User user);
    List<Pin> findByUserId(Long userId);
    List<Pin> findByRatingBetween(Double minRating, Double maxRating);
    Page<Pin> findByRatingBetween(Double minRating, Double maxRating, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    Optional<Pin> findById(Long id);

    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    List<Pin> findAll();

    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    List<Pin> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);

    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    List<Pin> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    @Query(
        "select distinct p from Pin p left join p.tags t left join Location l with l.pin = p " +
        "where (:text is null or lower(p.description) like lower(concat('%', :text, '%')) " +
        "or lower(p.title) like lower(concat('%', :text, '%')) " +
        "or lower(l.nameplace) like lower(concat('%', :text, '%'))) " +
        "or (:tagNames is not null and t.name in :tagNames)"
    )
    Page<Pin> searchByDescriptionOrTags(@Param("text") String text,
                                       @Param("tagNames") java.util.List<String> tagNames,
                                       Pageable pageable);

    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    List<Pin> findByTags_NameIgnoreCase(String name, Pageable pageable);
    
    // Методы для админской панели
    int countByCreatedAtAfter(LocalDateTime date);
    int countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"likes", "comments", "likes.user", "comments.user"})
    Page<Pin> findByDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase(String description, String title, Pageable pageable);
}