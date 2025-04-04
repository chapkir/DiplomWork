package com.example.server.UsPinterest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-images-dir:profile-images}")
    private String profileImagesDir;

    private Path fileStorageLocation;
    private Path profileImagesLocation;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            this.profileImagesLocation = Paths.get(uploadDir, profileImagesDir).toAbsolutePath().normalize();

            logger.info("Initializing file storage. Main directory: {}", fileStorageLocation);
            logger.info("Profile images directory: {}", profileImagesLocation);

            if (!Files.exists(fileStorageLocation)) {
                logger.info("Creating main upload directory: {}", fileStorageLocation);
                Files.createDirectories(fileStorageLocation);
            }

            if (!Files.exists(profileImagesLocation)) {
                logger.info("Creating profile images directory: {}", profileImagesLocation);
                Files.createDirectories(profileImagesLocation);
            }

            logger.info("File storage initialized successfully");
        } catch (IOException ex) {
            logger.error("Could not create directories for file storage: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not create directories for file storage", ex);
        }
    }

    public void checkAndCreateStorageDirectories() throws IOException {
        logger.info("Checking and creating storage directories if needed");
        logger.info("Main upload directory: {}", fileStorageLocation);
        logger.info("Profile images directory: {}", profileImagesLocation);

        if (fileStorageLocation == null) {
            fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            logger.info("fileStorageLocation was null, now set to: {}", fileStorageLocation);
        }

        if (profileImagesLocation == null) {
            profileImagesLocation = Paths.get(uploadDir, profileImagesDir).toAbsolutePath().normalize();
            logger.info("profileImagesLocation was null, now set to: {}", profileImagesLocation);
        }

        if (!Files.exists(fileStorageLocation)) {
            logger.info("Creating main upload directory: {}", fileStorageLocation);
            Files.createDirectories(fileStorageLocation);
        } else {
            logger.info("Main upload directory already exists");
        }

        if (!Files.exists(profileImagesLocation)) {
            logger.info("Creating profile images directory: {}", profileImagesLocation);
            Files.createDirectories(profileImagesLocation);
        } else {
            logger.info("Profile images directory already exists");
        }
    }

    public String storeFile(MultipartFile file, String customFilename) throws IOException {
        logger.info("Using method with customFilename: {}", customFilename);
        try {
            if (file == null) {
                logger.error("File is null");
                throw new IOException("File is null");
            }

            // Подробное логирование для отладки
            logger.info("=== НАЧАЛО СОХРАНЕНИЯ ФАЙЛА (С ПОЛЬЗОВАТЕЛЬСКИМ ИМЕНЕМ) ===");
            logger.info("Оригинальное имя файла: {}", file.getOriginalFilename());
            logger.info("Пользовательское имя файла: {}", customFilename);
            logger.info("Тип содержимого: {}", file.getContentType());
            logger.info("Размер файла: {} байт", file.getSize());

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
            logger.info("Очищенное имя файла: {}", originalFilename);

            // Проверяем существование директории для загрузки
            if (!Files.exists(fileStorageLocation)) {
                logger.info("Директория для сохранения не существует, создаём: {}", fileStorageLocation);
                Files.createDirectories(fileStorageLocation);
            } else {
                logger.info("Директория уже существует: {}", fileStorageLocation);
                logger.info("Директория доступна для записи: {}", Files.isWritable(fileStorageLocation));
            }

            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                logger.info("Расширение файла: {}", fileExtension);
            } else {
                logger.info("Файл без расширения");
            }

            String filename = (customFilename != null)
                    ? customFilename + fileExtension
                    : UUID.randomUUID().toString() + fileExtension;
            logger.info("Итоговое имя файла: {}", filename);

            Path targetLocation = fileStorageLocation.resolve(filename);
            logger.info("Полный путь для сохранения: {}", targetLocation);

            try {
                logger.info("Копирование файла...");
                InputStream inputStream = file.getInputStream();
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                inputStream.close();
                logger.info("Файл успешно сохранён по пути: {}", targetLocation);

                // Проверка, что файл действительно создан
                if (Files.exists(targetLocation)) {
                    logger.info("Файл существует после сохранения, размер: {} байт", Files.size(targetLocation));
                } else {
                    logger.error("ОШИБКА: Файл не был создан по пути: {}", targetLocation);
                }
            } catch (IOException e) {
                logger.error("Ошибка при сохранении файла: {}", e.getMessage(), e);
                throw new IOException("Ошибка при сохранении файла: " + e.getMessage(), e);
            }

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(filename)
                    .toUriString();
            logger.info("Сгенерированный URL файла: {}", fileUrl);
            logger.info("=== ЗАВЕРШЕНО СОХРАНЕНИЕ ФАЙЛА (С ПОЛЬЗОВАТЕЛЬСКИМ ИМЕНЕМ) ===");

            return fileUrl;
        } catch (Exception e) {
            logger.error("=== ОШИБКА ПРИ СОХРАНЕНИИ ФАЙЛА (С ПОЛЬЗОВАТЕЛЬСКИМ ИМЕНЕМ) ===", e);
            throw new IOException("Ошибка при сохранении файла: " + e.getMessage(), e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, null);
    }

    public String storeProfileImage(MultipartFile file, Long userId) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "user_" + userId + fileExtension;
        Path targetLocation = profileImagesLocation.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/profiles/")
                .path(filename)
                .toUriString();
    }

    public String getFilenameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        String[] parts = imageUrl.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    public String updateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }

        try {
            String currentContextPath = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

            if (imageUrl.startsWith(currentContextPath)) {
                return imageUrl;
            }

            if (imageUrl.startsWith("http")) {
                return imageUrl;
            }

            String filename = getFilenameFromUrl(imageUrl);
            if (filename != null) {
                if (imageUrl.contains("/profiles/")) {
                    return ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/profiles/")
                            .path(filename)
                            .toUriString();
                } else {
                    return ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/")
                            .path(filename)
                            .toUriString();
                }
            }

            return imageUrl;
        } catch (Exception e) {
            logger.error("Error updating image URL: {}", e.getMessage());
            return imageUrl;
        }
    }
}