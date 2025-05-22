package com.example.server.UsPinterest.service;

import net.coobird.thumbnailator.Thumbnails;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessor {

    public BufferedImage readImage(byte[] fileBytes) throws IOException {
        try (InputStream in = new ByteArrayInputStream(fileBytes)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Failed to read image");
            }
            return applyExifOrientation(fileBytes, img);
        }
    }

    public int[] calculateDimensions(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        // Сохраняем исходные пропорции изображения
        double originalAspectRatio = originalWidth / (double) originalHeight;
        
        // Вычисляем размеры с сохранением пропорций в пределах максимальных значений
        double widthScale = maxWidth / (double) originalWidth;
        double heightScale = maxHeight / (double) originalHeight;
        double scale = Math.min(widthScale, heightScale);
        
        int newWidth = (int) Math.round(originalWidth * scale);
        int newHeight = (int) Math.round(originalHeight * scale);
        
        return new int[]{newWidth, newHeight};
    }

    public BufferedImage resizeAndConvertToWebP(BufferedImage img, int width, int height) throws IOException {
        // Проверяем соответствие соотношения сторон 3:4
        double targetAspectRatio = 3.0 / 4.0;
        
        // Определяем новые размеры в соотношении 3:4, максимально заполняя доступное пространство
        int targetWidth = width;
        int targetHeight = height;
        
        // Убеждаемся, что изображение будет покрывать всё доступное пространство
        // Используем стратегию масштабирования, при которой обрезаются лишние части изображения
        return Thumbnails.of(img)
                .size(targetWidth, targetHeight)
                .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                .keepAspectRatio(false)
                .outputFormat("webp")
                .asBufferedImage();
    }

    private BufferedImage applyExifOrientation(byte[] imageBytes, BufferedImage image) {
        try (InputStream metaIn = new ByteArrayInputStream(imageBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(metaIn);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
                switch (orientation) {
                    case 3:
                        transform.rotate(Math.toRadians(180), image.getWidth()/2.0, image.getHeight()/2.0);
                        break;
                    case 6:
                        transform.rotate(Math.toRadians(90), image.getWidth()/2.0, image.getHeight()/2.0);
                        break;
                    case 8:
                        transform.rotate(Math.toRadians(270), image.getWidth()/2.0, image.getHeight()/2.0);
                        break;
                    default:
                        return image;
                }
                java.awt.image.AffineTransformOp op =
                        new java.awt.image.AffineTransformOp(transform, java.awt.image.AffineTransformOp.TYPE_BILINEAR);
                return op.filter(image, null);
            }
        } catch (Exception e) {
            // Игнорируем ошибки ориентации
        }
        return image;
    }

    /**
     * Рассчитывает размеры с соблюдением соотношения сторон 3:4
     * @param originalWidth Исходная ширина
     * @param originalHeight Исходная высота
     * @param maxWidth Максимальная ширина
     * @param maxHeight Максимальная высота
     * @return Массив с новой шириной и высотой
     */
    public int[] calculateAspectRatio3x4Dimensions(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        // Соотношение сторон 3:4
        final double targetAspectRatio = 3.0 / 4.0;
        
        // Вычисляем размеры для полного покрытия области
        // Стратегия: вписываем большую сторону по максимуму
        int resultWidth, resultHeight;
        
        // Всегда используем максимально возможные размеры для заполнения всей области
        resultWidth = maxWidth;
        resultHeight = (int) Math.round(resultWidth / targetAspectRatio);
        
        // Если высота превышает максимальную, корректируем обе стороны
        if (resultHeight > maxHeight) {
            resultHeight = maxHeight;
            resultWidth = (int) Math.round(resultHeight * targetAspectRatio);
        }
        
        return new int[]{resultWidth, resultHeight};
    }
}