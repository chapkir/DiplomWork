package com.example.server.UsPinterest.repository;

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
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    List<User> findByUsernameContainingIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(*) FROM follows WHERE following_id = :userId", nativeQuery = true)
    long countFollowersByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM follows WHERE follower_id = :userId", nativeQuery = true)
    long countFollowingByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.boards WHERE u.id = :id")
    Optional<User> findByIdWithBoards(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.comments WHERE u.id = :id")
    Optional<User> findByIdWithComments(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.likes WHERE u.id = :id")
    Optional<User> findByIdWithLikes(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.boards WHERE u.username = :username")
    Optional<User> findByUsernameWithBoards(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.comments WHERE u.username = :username")
    Optional<User> findByUsernameWithComments(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.likes WHERE u.username = :username")
    Optional<User> findByUsernameWithLikes(@Param("username") String username);

    // Проверка существования имени пользователя без учёта регистра
    boolean existsByUsernameIgnoreCase(String username);
}