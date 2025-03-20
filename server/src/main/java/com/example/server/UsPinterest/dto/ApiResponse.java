package com.example.server.UsPinterest.dto;

import java.time.LocalDateTime;

/**
 * Стандартный формат ответа API для успешных и неуспешных запросов
 * @param <T> тип данных в ответе
 */
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Создает успешный ответ с данными
     *
     * @param data данные ответа
     * @param message сообщение об успехе
     * @param <T> тип данных
     * @return объект ответа API
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "success";
        response.message = message;
        response.data = data;
        return response;
    }

    /**
     * Создает успешный ответ с данными и сообщением по умолчанию
     *
     * @param data данные ответа
     * @param <T> тип данных
     * @return объект ответа API
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Операция выполнена успешно");
    }

    /**
     * Создает успешный ответ без данных
     *
     * @param message сообщение об успехе
     * @return объект ответа API
     */
    public static ApiResponse<Void> success(String message) {
        return success(null, message);
    }

    /**
     * Создает ответ с ошибкой
     *
     * @param message сообщение об ошибке
     * @param <T> тип данных
     * @return объект ответа API
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "error";
        response.message = message;
        return response;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}