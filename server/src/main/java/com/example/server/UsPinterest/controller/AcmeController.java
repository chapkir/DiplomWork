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
import java.io.File;
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
                logger.info("Создана директория для ACME-challenge: {}", acmeDir);
            }
        } catch (IOException e) {
            logger.error("Ошибка при создании директории ACME: {}", e.getMessage(), e);
        }
        return acmeDir;
    }

    /**
     * Обрабатывает запросы валидации от Let's Encrypt
     * @param token Токен для проверки
     * @return Содержимое токена, если он существует
     */
    @GetMapping("/.well-known/acme-challenge/{token}")
    public String getToken(@PathVariable String token) {
        logger.info("Получен запрос ACME-проверки для токена: {}", token);

        // Сначала проверяем файл на диске
        Path tokenFile = getAcmeChallengeDir().resolve(token);
        if (Files.exists(tokenFile)) {
            try {
                String value = Files.readString(tokenFile);
                logger.info("Возвращаем значение из файла: {}", value);
                return value;
            } catch (IOException e) {
                logger.error("Ошибка при чтении файла токена: {}", e.getMessage(), e);
            }
        }

        // Затем проверяем in-memory хранилище
        if (tokens.containsKey(token)) {
            String value = tokens.get(token);
            logger.info("Возвращаем сохраненное значение из памяти: {}", value);

            // Сохраняем его в файл для надежности
            try {
                saveTokenToFile(token, value);
            } catch (IOException e) {
                logger.warn("Не удалось сохранить токен в файл: {}", e.getMessage());
            }

            return value;
        }

        // Если нет сохраненного значения, возвращаем сообщение об ошибке
        logger.warn("Токен не найден ни в файле, ни в памяти: {}", token);
        return "Token not found";
    }

    /**
     * Метод для сохранения токена и соответствующего ключа авторизации
     * @param token Токен
     * @param keyAuth Ключ авторизации
     * @return Сообщение об успешном сохранении
     */
    @PostMapping("/save-acme-token")
    public ResponseEntity<String> saveToken(@RequestParam String token, @RequestParam String keyAuth) {
        logger.info("Сохранение токена: {} с значением: {}", token, keyAuth);
        tokens.put(token, keyAuth);

        try {
            saveTokenToFile(token, keyAuth);
            return ResponseEntity.ok("Токен сохранен в памяти и файловой системе");
        } catch (IOException e) {
            logger.error("Ошибка при сохранении токена в файл: {}", e.getMessage(), e);
            return ResponseEntity.ok("Токен сохранен только в памяти. Ошибка файла: " + e.getMessage());
        }
    }

    /**
     * Сохраняет токен в файл
     */
    private void saveTokenToFile(String token, String keyAuth) throws IOException {
        Path tokenFile = getAcmeChallengeDir().resolve(token);
        Files.writeString(tokenFile, keyAuth, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.info("Токен сохранен в файл: {}", tokenFile);
    }

    /**
     * Метод для получения всех текущих токенов (для страницы администрирования)
     * @return Список всех токенов
     */
    @GetMapping("/acme-tokens")
    public ResponseEntity<Map<String, String>> getAllTokens() {
        logger.info("Запрос на получение всех токенов");

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
                        logger.error("Ошибка при чтении файла токена: {}", e.getMessage(), e);
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении директории токенов: {}", e.getMessage(), e);
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
        logger.info("Программно добавлен токен: {} со значением: {}", token, keyAuth);

        try {
            saveTokenToFile(token, keyAuth);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении программно добавленного токена в файл: {}", e.getMessage(), e);
        }
    }
}