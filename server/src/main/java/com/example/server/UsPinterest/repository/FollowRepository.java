package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.Follow;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {


    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    Page<Follow> findByFollower(User follower, Pageable pageable);
    Page<Follow> findByFollowing(User following, Pageable pageable);
    long countByFollower(User follower);

    long countByFollowing(User following);

    long deleteByFollowerAndFollowing(User follower, User following);
}