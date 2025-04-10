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

            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(profileImagesLocation);

            logger.info("File storage initialized successfully");
        } catch (IOException ex) {
            logger.error("Could not create directories for file storage: {}", ex.getMessage());
            throw new RuntimeException("Could not create directories for file storage", ex);
        }
    }

    public String storeFile(MultipartFile file, String customFilename) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ?
                file.getOriginalFilename() : "unknown");

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = (customFilename != null)
                ? customFilename + fileExtension
                : UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = fileStorageLocation.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();
    }

    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, null);
    }

    public String storeProfileImage(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Failed to store empty profile image");
        }

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
                .path("/uploads/profile-images/")
                .path(filename)
                .toUriString();
    }

    public String getFilenameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        String[] parts = imageUrl.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    public String updateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }

        if (imageUrl.startsWith(ServletUriComponentsBuilder.fromCurrentContextPath().toUriString())) {
            return imageUrl;
        }

        if (imageUrl.startsWith("/")) {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(imageUrl)
                    .toUriString();
        }

        return imageUrl;
    }

    public Path getFileStoragePath() {
        return fileStorageLocation;
    }

    public Path getProfileImagesPath() {
        return profileImagesLocation;
    }
}