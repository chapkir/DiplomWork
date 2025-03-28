package com.example.server.UsPinterest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Detailed error response for validation and other specific errors
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    /**
     * Error timestamp
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error type
     */
    private String error;

    /**
     * Error message
     */
    private String message;

    /**
     * Request path where error occurred
     */
    private String path;

    /**
     * Detailed field validation errors
     */
    private Map<String, String> fieldErrors;
}