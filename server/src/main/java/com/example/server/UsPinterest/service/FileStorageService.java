package com.example.server.UsPinterest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ImageProcessor imageProcessor;

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.fullhd-images-dir}")
    private String fullhdImagesDir;

    @Value("${file.thumbnail-images-dir}")
    private String thumbnailImagesDir;

    @Value("${file.profile-images-dir:profile-images}")
    private String profileImagesDir;

    @Value("${file.fullhd.max-width}")
    private int fullhdMaxWidth;

    @Value("${file.fullhd.max-height}")
    private int fullhdMaxHeight;

    @Value("${file.thumbnail.max-width}")
    private int thumbnailMaxWidth;

    @Value("${file.thumbnail.max-height}")
    private int thumbnailMaxHeight;

    @Value("${app.url}")
    private String appUrl;

    private Path fileStorageLocation;
    private Path fullhdImagesLocation;
    private Path thumbnailImagesLocation;
    private Path profileImagesLocation;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            this.fullhdImagesLocation = Paths.get(uploadDir, fullhdImagesDir).toAbsolutePath().normalize();
            this.thumbnailImagesLocation = Paths.get(uploadDir, thumbnailImagesDir).toAbsolutePath().normalize();
            this.profileImagesLocation = Paths.get(uploadDir, profileImagesDir).toAbsolutePath().normalize();

            logger.info("Инициализация хранилища файлов. Основная директория: {}", fileStorageLocation);
            logger.info("Директория FullHD изображений: {}", fullhdImagesLocation);
            logger.info("Директория миниатюр: {}", thumbnailImagesLocation);
            logger.info("Директория изображений профиля: {}", profileImagesLocation);

            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(fullhdImagesLocation);
            Files.createDirectories(thumbnailImagesLocation);
            Files.createDirectories(profileImagesLocation);

            logger.info("Хранилище файлов успешно инициализировано");
        } catch (IOException ex) {
            logger.error("Не удалось создать директории для хранения файлов: {}", ex.getMessage());
            throw new RuntimeException("Не удалось создать директории для хранения файлов", ex);
        }
    }

    /**
     * Сохраняет файл в основное хранилище без обработки
     * @param file файл для сохранения
     * @param customFilename пользовательское имя файла (опционально)
     * @return URL для доступа к файлу
     */
    public String storeFile(MultipartFile file, String customFilename) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Не удалось сохранить пустой файл");
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

        // Для изображений также автоматически создаем FullHD и миниатюру
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            try {
                // Создаем версии асинхронно, чтобы не задерживать ответ пользователю
                storeFullhdFileAsync(file, filename.substring(0, filename.lastIndexOf('.')));
                storeThumbnailFileAsync(file, filename.substring(0, filename.lastIndexOf('.')));
            } catch (Exception e) {
                logger.warn("Не удалось создать дополнительные версии изображения: {}", e.getMessage());
            }
        }

        return appUrl + "/uploads/" + filename;
    }

    /**
     * Сохраняет файл с автоматически сгенерированным именем
     */
    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, null);
    }

    /**
     * Сохраняет изображение профиля пользователя
     * @param file файл изображения
     * @param userId ID пользователя
     * @return URL к изображению профиля
     */
    public String storeProfileImage(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Не удалось сохранить пустое изображение профиля");
        }

        // Читаем изображение напрямую с правильными пропорциями
        byte[] fileBytes = file.getBytes();
        BufferedImage img = readImageWithCorrectDimensions(fileBytes, file.getContentType());

        if (img == null) {
            throw new IOException("Не удалось прочитать изображение профиля");
        }

        logger.info("storeProfileImage: оригинал {}x{}", img.getWidth(), img.getHeight());

        // Формируем имя файла с расширением .webp
        String filename = "user_" + userId + ".webp";
        Path targetLocation = profileImagesLocation.resolve(filename);

        // Масштабируем с сохранением пропорций под максимальные размеры миниатюры
        BufferedImage outImg = Thumbnails.of(img)
                .size(thumbnailMaxWidth, thumbnailMaxHeight)
                .keepAspectRatio(true)
                .outputFormat("webp")
                .asBufferedImage();

        logger.info("storeProfileImage: результат {}x{}, соотношение {}",
                outImg.getWidth(), outImg.getHeight(),
                String.format("%.2f", outImg.getWidth() / (double) outImg.getHeight()));

        // Записываем файл
        try (OutputStream os = Files.newOutputStream(targetLocation)) {
            ImageIO.write(outImg, "webp", os);
        }

        return appUrl + "/uploads/" + profileImagesDir + "/" + filename;
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

        // если URL абсолютный - оставляем его
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // Исправляем URL, чтобы использовать localhost вместо chapkir.com
            if (imageUrl.contains("chapkir.com:8081")) {
                return imageUrl.replace("chapkir.com:8081", "localhost:8081");
            }
            return imageUrl;
        }

        // конвертировать пути /uploads/* в /api/files/* для доступа через контроллер
        String path;
        if (imageUrl.contains("/uploads/fullhd/")) {
            path = imageUrl.replace("/uploads/fullhd/", "/api/files/fullhd/");
        } else if (imageUrl.contains("/uploads/" + profileImagesDir + "/")) {
            path = imageUrl.replace("/uploads/" + profileImagesDir + "/", "/api/files/profile-images/");
        } else if (imageUrl.contains("/uploads/")) {
            path = imageUrl.replace("/uploads/", "/api/files/");
        } else {
            path = imageUrl;
        }

        // проверяем относительный путь - он должен начинаться с /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // конвертируем в абсолютный URL с хостом
        // Используем localhost вместо удаленного хоста
        String absoluteUrl = "http://localhost:8081" + path;

        logger.debug("Преобразование URL: {} -> {}", imageUrl, absoluteUrl);
        return absoluteUrl;
    }

    public Path getFileStoragePath() {
        return fileStorageLocation;
    }

    public Path getFullhdImagesLocation() {
        return fullhdImagesLocation;
    }

    public Path getThumbnailImagesLocation() {
        return thumbnailImagesLocation;
    }

    public Path getProfileImagesLocation() {
        return profileImagesLocation;
    }

    public String getFullhdImagesDir() {
        return fullhdImagesDir;
    }

    public String getThumbnailImagesDir() {
        return thumbnailImagesDir;
    }

    public String getProfileImagesDir() {
        return profileImagesDir;
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

    /**
     * Более надежное чтение изображения с сохранением правильных пропорций
     */
    private BufferedImage readImageWithCorrectDimensions(byte[] fileBytes, String contentType) throws IOException {
        if (fileBytes == null || fileBytes.length == 0) {
            return null;
        }

        BufferedImage image = null;

        // Пытаемся прочитать изображение напрямую
        try (ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes)) {
            image = ImageIO.read(bis);
        } catch (Exception e) {
            logger.warn("Не удалось прочитать изображение стандартным способом: {}", e.getMessage());
        }

        // Если не удалось прочитать или изображение кажется квадратным, попробуем более точный метод
        if (image == null || (Math.abs(image.getWidth() - image.getHeight()) < 10 && contentType != null)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes);
                 ImageInputStream iis = ImageIO.createImageInputStream(bis)) {

                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(iis);
                        image = reader.read(0);
                        logger.info("Изображение прочитано специальным способом: {}x{}",
                                image.getWidth(), image.getHeight());
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (Exception e) {
                logger.warn("Не удалось прочитать изображение специальным способом: {}", e.getMessage());
            }
        }

        // Если до сих пор не удалось получить изображение, вернем null
        if (image == null) {
            return null;
        }

        // Применяем EXIF ориентацию
        return applyExifOrientation(fileBytes, image);
    }

    public ImageInfo storeResizedImage(MultipartFile file, String customFilename, Path targetDir, String uriPath, int maxWidth, int maxHeight) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Не удалось сохранить пустой файл");
        }
        byte[] fileBytes = file.getBytes();
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String baseName = customFilename != null ? customFilename : (originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : originalFilename);
        String filename = baseName + ".webp";
        Path targetLocation = targetDir.resolve(filename);

        // Читаем изображение напрямую с правильными пропорциями
        BufferedImage img = readImageWithCorrectDimensions(fileBytes, file.getContentType());

        if (img == null) {
            throw new IOException("Не удалось прочитать изображение для обработки");
        }

        logger.info("storeResizedImage: оригинал {}x{}, целевой размер {}x{}",
                img.getWidth(), img.getHeight(), maxWidth, maxHeight);

        // Масштабируем изображение, вписываясь в рамки, сохраняя пропорции
        BufferedImage outImg = Thumbnails.of(img)
                .size(maxWidth, maxHeight)
                .keepAspectRatio(true)
                .outputFormat("webp")
                .asBufferedImage();

        logger.info("storeResizedImage: результат {}x{}, соотношение {}",
                outImg.getWidth(), outImg.getHeight(),
                String.format("%.2f", outImg.getWidth() / (double) outImg.getHeight()));

        try (OutputStream os = Files.newOutputStream(targetLocation)) {
            ImageIO.write(outImg, "webp", os);
        }
        String url = appUrl + uriPath + filename;
        return new ImageInfo(url, outImg.getWidth(), outImg.getHeight());
    }

    public ImageInfo storeFullhdFile(MultipartFile file, String customFilename) throws IOException {
        // Сохраняем изображение в FullHD качестве с сохранением исходных пропорций
        ImageInfo info = storeResizedImage(file, customFilename, fullhdImagesLocation,
                "/uploads/" + fullhdImagesDir + "/", fullhdMaxWidth, fullhdMaxHeight);
        String filename = getFilenameFromUrl(info.getUrl());
        String url = appUrl + "/uploads/" + fullhdImagesDir + "/" + filename;
        return new ImageInfo(url, info.getWidth(), info.getHeight());
    }

    public ImageInfo storeThumbnailFile(MultipartFile file, String customFilename) throws IOException {
        // Создаем миниатюру с сохранением исходных пропорций
        ImageInfo info = storeResizedImage(file, customFilename, thumbnailImagesLocation,
                "/uploads/" + thumbnailImagesDir + "/", thumbnailMaxWidth, thumbnailMaxHeight);
        String filename = getFilenameFromUrl(info.getUrl());
        String url = appUrl + "/uploads/" + thumbnailImagesDir + "/" + filename;
        return new ImageInfo(url, info.getWidth(), info.getHeight());
    }

    /**
     * Асинхронная генерация FullHD варианта изображения
     */
    @Async("imageProcessingExecutor")
    public CompletableFuture<ImageInfo> storeFullhdFileAsync(MultipartFile file, String customFilename) throws IOException {
        ImageInfo info = storeFullhdFile(file, customFilename);
        return CompletableFuture.completedFuture(info);
    }

    /**
     * Асинхронная генерация миниатюрного варианта изображения
     */
    @Async("imageProcessingExecutor")
    public CompletableFuture<ImageInfo> storeThumbnailFileAsync(MultipartFile file, String customFilename) throws IOException {
        ImageInfo info = storeThumbnailFile(file, customFilename);
        return CompletableFuture.completedFuture(info);
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
        if (image == null) return null;

        try (InputStream metaIn = new ByteArrayInputStream(imageBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(metaIn);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                logger.debug("EXIF ориентация: {}", orientation);

                int width = image.getWidth();
                int height = image.getHeight();
                boolean dimensionsSwapped = false;

                // Создаем трансформацию в зависимости от ориентации
                AffineTransform transform = new AffineTransform();

                switch (orientation) {
                    case 1: // Нормальная ориентация
                        return image;
                    case 2: // Отражение по горизонтали
                        transform.scale(-1.0, 1.0);
                        transform.translate(-width, 0);
                        break;
                    case 3: // Поворот на 180 градусов
                        transform.translate(width, height);
                        transform.rotate(Math.PI);
                        break;
                    case 4: // Отражение по вертикали
                        transform.scale(1.0, -1.0);
                        transform.translate(0, -height);
                        break;
                    case 5: // Отражение по горизонтали и поворот на 270 градусов
                        transform.rotate(-Math.PI / 2);
                        transform.scale(-1.0, 1.0);
                        dimensionsSwapped = true;
                        break;
                    case 6: // Поворот на 90 градусов
                        transform.translate(height, 0);
                        transform.rotate(Math.PI / 2);
                        dimensionsSwapped = true;
                        break;
                    case 7: // Отражение по горизонтали и поворот на 90 градусов
                        transform.scale(-1.0, 1.0);
                        transform.translate(-height, 0);
                        transform.translate(0, width);
                        transform.rotate(Math.PI / 2);
                        dimensionsSwapped = true;
                        break;
                    case 8: // Поворот на 270 градусов
                        transform.translate(0, width);
                        transform.rotate(-Math.PI / 2);
                        dimensionsSwapped = true;
                        break;
                    default:
                        return image;
                }

                // Создаем новое изображение правильных размеров после поворота
                BufferedImage result;
                if (dimensionsSwapped) {
                    result = new BufferedImage(height, width, image.getType());
                } else {
                    result = new BufferedImage(width, height, image.getType());
                }

                // Применяем трансформацию
                AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
                return op.filter(image, result);
            }
        } catch (Exception e) {
            logger.warn("Не удалось применить EXIF ориентацию: {}", e.getMessage());
        }
        return image;
    }

    /**
     * Удаляет физический файл по его URL, если он находится в локальном хранилище
     */
    public void deleteStoredFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        String filename = getFilenameFromUrl(fileUrl);
        if (filename == null) return;
        try {
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            logger.info("Файл удален из хранилища: {}", filePath);
        } catch (Exception e) {
            logger.warn("Не удалось удалить файл {}: {}", fileUrl, e.getMessage());
        }
    }
}