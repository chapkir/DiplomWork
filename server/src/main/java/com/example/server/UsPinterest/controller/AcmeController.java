package com.example.server.UsPinterest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.springframework.beans.factory.annotation.Value;

/**
 * Контроллер для обработки запросов валидации от Let's Encrypt (ACME протокол)
 */
@RestController
public class AcmeController {

    private static final Logger logger = LoggerFactory.getLogger(AcmeController.class);

    // Временное хранилище токенов для Let's Encrypt
    private final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();

    // Директория для сохранения файлов ACME-challenge
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path getAcmeChallengeDir() {
        Path acmeDir = Paths.get(uploadDir, ".well-known", "acme-challenge");
        try {
            if (!Files.exists(acmeDir)) {
                Files.createDirectories(acmeDir);
            }
        } catch (IOException e) {
            logger.error("Ошибка при создании директории ACME", e);
        }
        return acmeDir;
    }

    /**
     * Обрабатывает запросы валидации от Let's Encrypt
     */
    @GetMapping("/.well-known/acme-challenge/{token}")
    public String getToken(@PathVariable String token) {
        // Сначала проверяем файл на диске
        Path tokenFile = getAcmeChallengeDir().resolve(token);
        if (Files.exists(tokenFile)) {
            try {
                return Files.readString(tokenFile);
            } catch (IOException e) {
                logger.error("Ошибка при чтении файла токена", e);
            }
        }

        // Затем проверяем in-memory хранилище
        if (tokens.containsKey(token)) {
            String value = tokens.get(token);

            // Сохраняем его в файл для надежности
            try {
                saveTokenToFile(token, value);
            } catch (IOException e) {
                logger.warn("Не удалось сохранить токен в файл", e);
            }

            return value;
        }

        // Если нет сохраненного значения, возвращаем сообщение об ошибке
        return "Token not found";
    }

    /**
     * Метод для сохранения токена и соответствующего ключа авторизации
     */
    @PostMapping("/save-acme-token")
    public ResponseEntity<String> saveToken(@RequestParam String token, @RequestParam String keyAuth) {
        tokens.put(token, keyAuth);

        try {
            saveTokenToFile(token, keyAuth);
            return ResponseEntity.ok("Токен сохранен");
        } catch (IOException e) {
            logger.error("Ошибка при сохранении токена в файл", e);
            return ResponseEntity.ok("Токен сохранен только в памяти");
        }
    }

    /**
     * Сохраняет токен в файл
     */
    private void saveTokenToFile(String token, String keyAuth) throws IOException {
        Path tokenFile = getAcmeChallengeDir().resolve(token);
        Files.writeString(tokenFile, keyAuth, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Метод для получения всех текущих токенов (для страницы администрирования)
     */
    @GetMapping("/acme-tokens")
    public ResponseEntity<Map<String, String>> getAllTokens() {
        // Собираем токены из памяти
        Map<String, String> tokenMap = new HashMap<>(tokens);

        // Добавляем токены из файловой системы
        try {
            Path acmeDir = getAcmeChallengeDir();
            if (Files.exists(acmeDir)) {
                Files.list(acmeDir).forEach(file -> {
                    try {
                        String fileName = file.getFileName().toString();
                        String content = Files.readString(file);
                        tokenMap.put(fileName, content);
                    } catch (IOException e) {
                        logger.error("Ошибка при чтении файла токена", e);
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении директории токенов", e);
        }

        return ResponseEntity.ok(tokenMap);
    }

    /**
     * Тестовый метод для проверки работоспособности
     */
    @GetMapping("/acme-test")
    public String test() {
        return "ACME контроллер работает!";
    }

    /**
     * Метод для программного сохранения токена
     */
    public void addToken(String token, String keyAuth) {
        tokens.put(token, keyAuth);

        try {
            saveTokenToFile(token, keyAuth);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении токена в файл", e);
        }
    }
}