package com.example.server.UsPinterest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.ByteArrayInputStream;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-images-dir:profile-images}")
    private String profileImagesDir;

    @Value("${file.fullhd-images-dir}")
    private String fullhdImagesDir;

    @Value("${file.thumbnail-images-dir}")
    private String thumbnailImagesDir;

    @Value("${file.fullhd.max-width}")
    private int fullhdMaxWidth;

    @Value("${file.fullhd.max-height}")
    private int fullhdMaxHeight;

    @Value("${file.thumbnail.max-width}")
    private int thumbnailMaxWidth;

    @Value("${file.thumbnail.max-height}")
    private int thumbnailMaxHeight;

    private Path fileStorageLocation;
    private Path profileImagesLocation;
    private Path fullhdImagesLocation;
    private Path thumbnailImagesLocation;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            this.profileImagesLocation = Paths.get(uploadDir, profileImagesDir).toAbsolutePath().normalize();
            this.fullhdImagesLocation = Paths.get(uploadDir, fullhdImagesDir).toAbsolutePath().normalize();
            this.thumbnailImagesLocation = Paths.get(uploadDir, thumbnailImagesDir).toAbsolutePath().normalize();

            logger.info("Initializing file storage. Main directory: {}", fileStorageLocation);
            logger.info("Profile images directory: {}", profileImagesLocation);
            logger.info("FullHD images directory: {}", fullhdImagesLocation);
            logger.info("Thumbnail images directory: {}", thumbnailImagesLocation);

            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(profileImagesLocation);
            Files.createDirectories(fullhdImagesLocation);
            Files.createDirectories(thumbnailImagesLocation);

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
        // Читаем исходное изображение с применением ориентации
        byte[] fileBytes = file.getBytes();
        BufferedImage originalImg;
        try (InputStream imgIn = new ByteArrayInputStream(fileBytes)) {
            originalImg = ImageIO.read(imgIn);
        }
        // Применяем EXIF ориентацию
        originalImg = applyExifOrientation(fileBytes, originalImg);
        if (originalImg == null) {
            throw new IOException("Не удалось прочитать изображение для аватара");
        }
        // Формируем имя файла с расширением .webp
        String filename = "user_" + userId + ".webp";
        Path targetLocation = profileImagesLocation.resolve(filename);
        // Сжимаем и конвертируем в WebP с настройками миниатюры
        BufferedImage webpImg = Thumbnails.of(originalImg)
                .size(thumbnailMaxWidth, thumbnailMaxHeight)
                .outputFormat("webp")
                .asBufferedImage();
        ImageIO.write(webpImg, "webp", targetLocation.toFile());
        // Возвращаем URL к новому WebP-файлу
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

    public Path getFullhdImagesLocation() {
        return fullhdImagesLocation;
    }

    public Path getThumbnailImagesLocation() {
        return thumbnailImagesLocation;
    }

    public String getFullhdImagesDir() {
        return fullhdImagesDir;
    }

    public String getThumbnailImagesDir() {
        return thumbnailImagesDir;
    }

    public int getFullhdMaxWidth() {
        return fullhdMaxWidth;
    }

    public int getFullhdMaxHeight() {
        return fullhdMaxHeight;
    }

    public int getThumbnailMaxWidth() {
        return thumbnailMaxWidth;
    }

    public int getThumbnailMaxHeight() {
        return thumbnailMaxHeight;
    }


    public ImageInfo storeResizedImage(MultipartFile file, String customFilename, Path targetDir, String uriPath, int width, int height) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }
        ImageIO.scanForPlugins();
        byte[] fileBytes = file.getBytes();
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String baseName = customFilename != null ? customFilename : (originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : originalFilename);
        String filename = baseName + ".webp";
        Path targetLocation = targetDir.resolve(filename);
        BufferedImage img;
        try (InputStream imgIn = new ByteArrayInputStream(fileBytes)) {
            img = ImageIO.read(imgIn);
        }
        if (img == null) {
            throw new IOException("Failed to read image for resizing");
        }
        // Применяем EXIF ориентацию
        img = applyExifOrientation(fileBytes, img);
        BufferedImage outImg = Thumbnails.of(img).size(width, height).outputFormat("webp").asBufferedImage();
        try (OutputStream os = Files.newOutputStream(targetLocation)) {
            ImageIO.write(outImg, "webp", os);
        }
        String url = ServletUriComponentsBuilder.fromCurrentContextPath().path(uriPath).path(filename).toUriString();
        return new ImageInfo(url, outImg.getWidth(), outImg.getHeight());
    }

    public ImageInfo storeFullhdFile(MultipartFile file, String customFilename) throws IOException {
        return storeResizedImage(file, customFilename, fullhdImagesLocation, "/uploads/" + fullhdImagesDir + "/", fullhdMaxWidth, fullhdMaxHeight);
    }

    public ImageInfo storeThumbnailFile(MultipartFile file, String customFilename) throws IOException {
        return storeResizedImage(file, customFilename, thumbnailImagesLocation, "/uploads/" + thumbnailImagesDir + "/", thumbnailMaxWidth, thumbnailMaxHeight);
    }

    /**
     * Класс-холдер для информации о сохранённом изображении
     */
    public static class ImageInfo {
        private String url;
        private int width;
        private int height;

        public ImageInfo(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }
        public String getUrl() { return url; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }


    private BufferedImage applyExifOrientation(byte[] imageBytes, BufferedImage image) {
        try (InputStream metaIn = new ByteArrayInputStream(imageBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(metaIn);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                AffineTransform transform = new AffineTransform();
                switch (orientation) {
                    case 3:
                        transform.rotate(Math.toRadians(180), image.getWidth() / 2.0, image.getHeight() / 2.0);
                        break;
                    case 6:
                        transform.rotate(Math.toRadians(90), image.getWidth() / 2.0, image.getHeight() / 2.0);
                        break;
                    case 8:
                        transform.rotate(Math.toRadians(270), image.getWidth() / 2.0, image.getHeight() / 2.0);
                        break;
                    default:
                        return image;
                }
                AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
                return op.filter(image, null);
            }
        } catch (Exception e) {
            logger.warn("Не удалось применить EXIF ориентацию: {}", e.getMessage());
        }
        return image;
    }
}