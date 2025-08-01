package com.example.server.UsPinterest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String token, String message) {
        super(String.format("Ошибка для токена [%s]: %s", token, message));
    }

    public TokenRefreshException(String message) {
        super(message);
    }
}