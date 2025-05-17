package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.entity.FcmToken;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository repository;

    public void registerToken(User user, String token) {
        boolean exists = repository.findByUser(user).stream()
                .anyMatch(ft -> ft.getToken().equals(token));
        if (!exists) {
            FcmToken f = new FcmToken();
            f.setUser(user);
            f.setToken(token);
            repository.save(f);
        }
    }

    public void removeToken(User user, String token) {
        repository.deleteByUserAndToken(user, token);
    }

    public List<FcmToken> getTokensByUser(User user) {
        return repository.findByUser(user);
    }
} 