package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-images-dir:profile-images}")
    private String profileImagesDir;

    @Value("${file.fullhd-images-dir:fullhd}")
    private String fullhdImagesDir;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();

            if (!Files.exists(filePath)) {
                logger.warn("Файл не найден в корне: {}", filePath);
                // Попробуем найти в fullhd
                filePath = Paths.get(uploadDir, fullhdImagesDir).resolve(filename).normalize();
                if (!Files.exists(filePath)) {
                    logger.warn("Файл не найден в fullhd: {}", filePath);
                    return ResponseEntity.notFound().build();
                }
            }

            logger.info("Подаём файл: {} -> {}", filename, filePath);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(getMediaTypeForFile(filename))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            logger.error("Ошибка при формировании URL для файла: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profiles/{filename:.+}")
    public ResponseEntity<Resource> serveProfileImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, profileImagesDir).resolve(filename).normalize();

            if (!Files.exists(filePath)) {
                logger.warn("Файл профиля не найден: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            logger.info("Подаём файл профиля: {} -> {}", filename, filePath);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(getMediaTypeForFile(filename))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            logger.error("Ошибка при формировании URL для файла профиля: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugFileSystem() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Получаем список файлов в каждой директории
            Path rootPath = Paths.get(uploadDir);
            Path profilePath = Paths.get(uploadDir, profileImagesDir);
            Path fullhdPath = Paths.get(uploadDir, fullhdImagesDir);

            List<String> rootFiles = Files.list(rootPath)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());

            List<String> profileFiles = Files.exists(profilePath) ?
                    Files.list(profilePath)
                            .filter(Files::isRegularFile)
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList()) :
                    Collections.emptyList();

            List<String> fullhdFiles = Files.exists(fullhdPath) ?
                    Files.list(fullhdPath)
                            .filter(Files::isRegularFile)
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList()) :
                    Collections.emptyList();

            result.put("root_files_count", rootFiles.size());
            result.put("profile_files_count", profileFiles.size());
            result.put("fullhd_files_count", fullhdFiles.size());

            result.put("root_files", rootFiles.size() > 20 ? rootFiles.subList(0, 20) : rootFiles);
            result.put("profile_files", profileFiles.size() > 20 ? profileFiles.subList(0, 20) : profileFiles);
            result.put("fullhd_files", fullhdFiles.size() > 20 ? fullhdFiles.subList(0, 20) : fullhdFiles);

            result.put("root_path", rootPath.toString());
            result.put("profile_path", profilePath.toString());
            result.put("fullhd_path", fullhdPath.toString());

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/{type}/{filename:.+}")
    public ResponseEntity<Resource> serveFileByType(@PathVariable String type, @PathVariable String filename) {
        try {
            Path filePath;

            // Определяем путь в зависимости от типа
            if ("fullhd".equals(type)) {
                filePath = Paths.get(uploadDir, fullhdImagesDir, filename).normalize();
                if (!Files.exists(filePath)) {
                    // Пробуем найти в корне uploads
                    filePath = Paths.get(uploadDir, filename).normalize();
                }
            } else if ("profile-images".equals(type)) {
                filePath = Paths.get(uploadDir, profileImagesDir, filename).normalize();
                if (!Files.exists(filePath)) {
                    // Пробуем найти в корне uploads
                    filePath = Paths.get(uploadDir, filename).normalize();
                }
            } else {
                // Для других типов ищем в указанной папке
                filePath = Paths.get(uploadDir, type, filename).normalize();
                if (!Files.exists(filePath)) {
                    filePath = Paths.get(uploadDir, filename).normalize();
                }
            }

            if (!Files.exists(filePath)) {
                logger.warn("Файл не найден нигде: {}/{}", type, filename);
                return ResponseEntity.notFound().build();
            }

            logger.info("Найден файл по пути {}/{}: {}", type, filename, filePath);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(getMediaTypeForFile(filename))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            logger.error("Ошибка при формировании URL для файла: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private MediaType getMediaTypeForFile(String filename) {
        String extension = "";
        if (filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }

        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "webp":
                return MediaType.valueOf("image/webp");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}