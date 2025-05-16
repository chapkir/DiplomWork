package com.example.server.UsPinterest.repository;

import com.example.server.UsPinterest.model.VerificationToken;
import com.example.server.UsPinterest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    List<VerificationToken> findByUser(User user);
} 