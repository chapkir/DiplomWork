package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.model.Photo;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);

    int countByPostId(Long postId);

    // Для совместимости с Pin
    List<Like> findByUser(User user);

    // Получение лайков пользователя с сортировкой по дате создания (сначала новые)
    @Query("SELECT l FROM Like l WHERE l.user = ?1 ORDER BY l.id DESC")
    List<Like> findByUserOrderByIdDesc(User user);

    Optional<Like> findByPinAndUser(Pin pin, User user);

    // Подсчёт лайков для пина
    int countByPinId(Long pinId);

    // Для совместимости с Photo
    Optional<Like> findByPhotoAndUser(Photo photo, User user);
} 