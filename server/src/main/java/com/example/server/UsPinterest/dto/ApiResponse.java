package com.example.server.UsPinterest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response format for all REST endpoints
 * @param <T> Type of data payload
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the operation was successful
     */
    private boolean success;

    /**
     * Response message
     */
    private String message;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Request path
     */
    private String path;

    /**
     * Response timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Data payload
     */
    private T data;

    /**
     * Creates a success response with data
     *
     * @param data Data payload
     * @param <T> Type of data
     * @return ApiResponse with success flag set to true
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setStatus(200);
        response.setTimestamp(LocalDateTime.now());
        response.setData(data);
        return response;
    }

    /**
     * Creates a success response with message
     *
     * @param message Success message
     * @return ApiResponse with success flag set to true
     */
    public static ApiResponse<Void> success(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setStatus(200);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Creates an error response
     *
     * @param message Error message
     * @return ApiResponse with success flag set to false
     */
    public static ApiResponse<Void> error(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus(400);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Creates an error response with specific status code
     *
     * @param message Error message
     * @param status HTTP status code
     * @return ApiResponse with success flag set to false and the specified status
     */
    public static ApiResponse<Void> error(String message, int status) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus(status);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Full constructor
     *
     * @param success Success flag
     * @param message Response message
     * @param status HTTP status code
     * @param path Request path
     */
    public ApiResponse(boolean success, String message, int status, String path) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Full constructor with data
     *
     * @param success Success flag
     * @param message Response message
     * @param status HTTP status code
     * @param path Request path
     * @param data Data payload
     */
    public ApiResponse(boolean success, String message, int status, String path, T data) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}