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

/**
 * Service for storing files on local filesystem
 */
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

            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(profileImagesLocation);

            logger.info("File storage initialized at: {}", fileStorageLocation);
            logger.info("Profile images storage initialized at: {}", profileImagesLocation);
        } catch (IOException ex) {
            logger.error("Could not create file upload directories", ex);
            throw new RuntimeException("Could not create directories for file storage", ex);
        }
    }

    /**
     * Store a file and return its public URL
     *
     * @param file the file to store
     * @return the URL to access the file
     * @throws IOException if file operations fail
     */
    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, null);
    }

    /**
     * Store a file with a specific filename and return its public URL
     *
     * @param file the file to store
     * @param customFilename optional custom filename to use
     * @return the URL to access the file
     * @throws IOException if file operations fail
     */
    public String storeFile(MultipartFile file, String customFilename) throws IOException {
        // Normalize file name
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with original extension
        String filename = (customFilename != null)
                ? customFilename + fileExtension
                : UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = fileStorageLocation.resolve(filename);

        logger.debug("Storing file to: {}", targetLocation);

        // Copy file to the target location
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        // Build and return the download URL
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(filename)
                .toUriString();
    }

    /**
     * Store a profile image for a user and return its public URL
     *
     * @param file the profile image file
     * @param userId the user ID
     * @return the URL to access the profile image
     * @throws IOException if file operations fail
     */
    public String storeProfileImage(MultipartFile file, Long userId) throws IOException {
        // Generate filename from user ID
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "user_" + userId + fileExtension;
        Path targetLocation = profileImagesLocation.resolve(filename);

        logger.debug("Storing profile image to: {}", targetLocation);

        // Copy file to the target location
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        // Build and return the download URL
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/profiles/")
                .path(filename)
                .toUriString();
    }

    /**
     * Helper method to parse existing image URL to retrieve filename
     *
     * @param imageUrl the existing image URL
     * @return the file name part of the URL
     */
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

    /**
     * Updates an image URL to ensure it uses the correct host
     * (Used when migrating from another storage provider)
     *
     * @param imageUrl original image URL
     * @return updated image URL with current host
     */
    public String updateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }

        // If URL is already from our server, return as is
        String currentContextPath = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        if (imageUrl.startsWith(currentContextPath)) {
            return imageUrl;
        }

        // For external URLs, don't modify
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }

        // For relative paths, add server base URL
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
    }
}