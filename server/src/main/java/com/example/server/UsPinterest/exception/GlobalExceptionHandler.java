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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API
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

        logger.warn("Validation error: {}", errors);

        return new ResponseEntity<>(errorDetails, headers, status);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Resource not found at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle bad credentials exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Authentication failed at {}", path);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Неверный логин или пароль",
                HttpStatus.UNAUTHORIZED.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle token refresh exceptions
     */
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Token refresh error at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle general authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Authentication error at {}", path);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Ошибка аутентификации",
                HttpStatus.UNAUTHORIZED.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Access denied at {}", path);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Нет доступа к данному ресурсу",
                HttpStatus.FORBIDDEN.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle follow exceptions
     */
    @ExceptionHandler(FollowException.class)
    public ResponseEntity<ApiResponse<Void>> handleFollowException(FollowException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Follow error at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle data integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("Data integrity violation at {}: {}", path, ex.getMessage());

        String message;
        if (ex.getMostSpecificCause().getMessage().contains("duplicate key")) {
            message = "Запись с такими данными уже существует";
        } else {
            message = "Нарушение целостности данных";
        }

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                message,
                HttpStatus.CONFLICT.value(),
                path,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle multipart file upload exceptions
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex, WebRequest request) {
        String path = extractPath(request);
        logger.warn("File upload error at {}: {}", path, ex.getMessage());

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                "Ошибка при загрузке файла",
                HttpStatus.BAD_REQUEST.value(),
                path,
                null
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
                "Произошла неожиданная ошибка",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path,
                null
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
            logger.warn("Unable to extract request path", e);
        }
        return "unknown";
    }
}