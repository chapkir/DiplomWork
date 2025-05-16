package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {


    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    Page<Follow> findByFollower(User follower, Pageable pageable);
    Page<Follow> findByFollowing(User following, Pageable pageable);
    int countByFollower(User follower);

    int countByFollowing(User following);

    int deleteByFollowerAndFollowing(User follower, User following);
    int deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);


    List<Follow> findByFollowing(User following);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO follows (follower_id, following_id, created_at) VALUES (:followerId, :followingId, :createdAt)", nativeQuery = true)
    void createFollowManually(@Param("followerId") Long followerId, @Param("followingId") Long followingId, @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Transactional
    int deleteByFollowerId(Long followerId);

    @Modifying
    @Transactional
    int deleteByFollowingId(Long followingId);
}