package com.example.server.UsPinterest.exception;

/**
 * Исключение, возникающее при ошибках в операциях с подписками
 */
public class FollowException extends RuntimeException {

    public FollowException(String message) {
        super(message);
    }

    public FollowException(String message, Throwable cause) {
        super(message, cause);
    }
}