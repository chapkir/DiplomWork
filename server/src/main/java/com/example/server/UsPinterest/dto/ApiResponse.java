package com.example.server.UsPinterest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private int status;

    private String path;

    private LocalDateTime timestamp;

    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setStatus(200);
        response.setTimestamp(LocalDateTime.now());
        response.setData(data);
        return response;
    }

    public static ApiResponse<Void> error(String message, int status) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus(status);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public ApiResponse(boolean success, String message, int status, String path, T data) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}