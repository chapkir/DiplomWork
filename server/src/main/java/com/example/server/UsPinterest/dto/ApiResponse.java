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
     * Creates an error response
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
     * Constructor with all fields
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