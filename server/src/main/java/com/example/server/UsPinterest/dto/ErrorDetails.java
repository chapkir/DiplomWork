package com.example.server.UsPinterest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    private Map<String, String> fieldErrors;
}