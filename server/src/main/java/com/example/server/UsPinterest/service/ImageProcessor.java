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
        double aspectRatio = (double) originalWidth / originalHeight;
        int widthBound = maxWidth;
        int heightBound = maxHeight;
        if (originalHeight > originalWidth) {
            widthBound = maxHeight;
            heightBound = maxWidth;
        }
        int newWidth = widthBound;
        int newHeight = (int) (widthBound / aspectRatio);
        if (newHeight > heightBound) {
            newHeight = heightBound;
            newWidth = (int) (heightBound * aspectRatio);
        }
        return new int[]{newWidth, newHeight};
    }

    public BufferedImage resizeAndConvertToWebP(BufferedImage img, int width, int height) throws IOException {
        return Thumbnails.of(img)
                .size(width, height)
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
}