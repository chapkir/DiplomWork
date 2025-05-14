package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.security.UserPrincipal;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

@Service("customUserDetailsServiceService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        // Возвращаем UserPrincipal вместо стандартного User
        return new UserPrincipal(user);
    }
} 