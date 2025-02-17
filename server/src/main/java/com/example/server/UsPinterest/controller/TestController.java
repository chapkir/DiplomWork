package com.example.server.UsPinterest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        logger.info("Получен запрос к /api/test");
        
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "API работает!");
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Отправка успешного ответа: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке запроса: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 