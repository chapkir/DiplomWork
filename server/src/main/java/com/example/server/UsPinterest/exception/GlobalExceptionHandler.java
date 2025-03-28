package com.example.server.UsPinterest.exception;

import com.example.server.UsPinterest.dto.ApiResponse;
import com.example.server.UsPinterest.dto.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API
 * Provides consistent error responses across the application
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation exceptions with detailed field errors
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));

        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setTimestamp(LocalDateTime.now());
        errorDetails.setStatus(status.value());
        errorDetails.setError("Validation Failed");
        errorDetails.setMessage("Input validation failed");
        errorDetails.setPath(extractPath(request));
        errorDetails.setFieldErrors(errors);

        logger.error("Validation error: {}", errors);

        return new ResponseEntity<>(errorDetails, headers, status);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Resource not found at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle bad credentials exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Authentication failed at {}: {}", path, maskAuthenticationDetails(ex.getMessage()));

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Invalid username or password",
                HttpStatus.UNAUTHORIZED.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle general authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Authentication error at {}: {}", path, maskAuthenticationDetails(ex.getMessage()));

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Authentication failed",
                HttpStatus.UNAUTHORIZED.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Access denied at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "You don't have permission to access this resource",
                HttpStatus.FORBIDDEN.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle file system access denied exceptions
     */
    @ExceptionHandler(java.nio.file.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileAccessDeniedException(java.nio.file.AccessDeniedException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("File access denied at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "You don't have permission to access this resource",
                HttpStatus.FORBIDDEN.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle data integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Data integrity violation at {}: {}", path, ex.getMessage());

        // Extract useful information from the exception
        String message = ex.getMostSpecificCause().getMessage();

        // If it's a duplicate key violation, provide a clearer message
        if (message != null && message.contains("duplicate key")) {
            message = "A record with the same unique identifier already exists";
        } else {
            message = "Database constraint violation";
        }

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                message,
                HttpStatus.CONFLICT.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        String path = extractPath(request);

        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        logger.error("Constraint violation at {}: {}", path, message);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Validation failed: " + message,
                HttpStatus.BAD_REQUEST.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle multipart file upload exceptions
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Multipart file upload error at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Error processing the uploaded file",
                HttpStatus.BAD_REQUEST.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String path = extractPath(request);

        String message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");

        logger.error("Type mismatch at {}: {}", path, message);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                message,
                HttpStatus.BAD_REQUEST.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle global exceptions that weren't caught by other handlers
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        String path = extractPath(request);
        logger.error("Unexpected error at {}: {}", path, ex.getMessage(), ex);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extract request path from WebRequest
     */
    private String extractPath(WebRequest request) {
        try {
            if (request instanceof ServletWebRequest) {
                HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();
                return httpRequest.getRequestURI();
            }
        } catch (Exception e) {
            logger.warn("Unable to extract request path: {}", e.getMessage());
        }
        return "unknown";
    }

    /**
     * Mask sensitive information in authentication details
     */
    private String maskAuthenticationDetails(String message) {
        if (message == null) return "null";

        // Mask user names, emails, and any other potentially sensitive info
        return message.replaceAll("(username|email|login):\\s*[^\\s,]+", "$1: ***MASKED***")
                .replaceAll("(password):\\s*[^\\s,]+", "$1: ***MASKED***");
    }
}