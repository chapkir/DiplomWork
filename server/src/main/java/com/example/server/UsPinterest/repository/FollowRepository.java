package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с подписками
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * Проверяет наличие подписки
     *
     * @param follower подписчик
     * @param following пользователь, на которого подписались
     * @return экземпляр подписки или пустой Optional
     */
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    /**
     * Поиск пользователей, на которых подписан указанный пользователь
     *
     * @param follower подписчик
     * @param pageable параметры пагинации
     * @return список подписок с пагинацией
     */
    Page<Follow> findByFollower(User follower, Pageable pageable);

    /**
     * Поиск подписчиков указанного пользователя
     *
     * @param following пользователь, чьих подписчиков ищем
     * @param pageable параметры пагинации
     * @return список подписок с пагинацией
     */
    Page<Follow> findByFollowing(User following, Pageable pageable);

    /**
     * Считает количество подписок пользователя
     *
     * @param follower пользователь, подписки которого считаем
     * @return количество подписок
     */
    long countByFollower(User follower);

    /**
     * Считает количество подписчиков пользователя
     *
     * @param following пользователь, подписчиков которого считаем
     * @return количество подписчиков
     */
    long countByFollowing(User following);

    /**
     * Удаляет подписку между двумя пользователями
     *
     * @param follower подписчик
     * @param following пользователь, на которого подписались
     * @return количество удаленных записей
     */
    long deleteByFollowerAndFollowing(User follower, User following);
}