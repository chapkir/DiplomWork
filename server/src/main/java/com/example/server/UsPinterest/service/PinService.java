package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.PinFullHdResponse;
import com.example.server.UsPinterest.dto.PinThumbnailResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.annotation.Timed;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.server.UsPinterest.service.FileStorageService;
import com.example.server.UsPinterest.dto.CommentResponse;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.service.PaginationService;
import java.net.URL;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import java.io.File;
import java.io.InputStream;
import com.drew.metadata.jpeg.JpegDirectory;
import java.io.FileInputStream;
import java.nio.file.Files;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.example.server.UsPinterest.dto.mapper.PinStructMapper;
import com.example.server.UsPinterest.dto.mapper.PinFullHdStructMapper;
import com.example.server.UsPinterest.dto.mapper.PinThumbnailStructMapper;
import org.springframework.context.ApplicationEventPublisher;

@Service
@Transactional
@Timed(value = "pins.service", description = "Metrics for PinService methods")
public class PinService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final Logger logger = LoggerFactory.getLogger(PinService.class);

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PaginationService paginationService;

    @Autowired
    private PinStructMapper pinStructMapper;

    @Autowired
    private PinFullHdStructMapper pinFullHdStructMapper;

    @Autowired
    private PinThumbnailStructMapper pinThumbnailStructMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Cacheable(value = "pins", key = "#id")
    public Optional<Pin> getPinById(Long id) {
        logger.debug("Загрузка пина с ID {} из базы данных", id);
        return pinRepository.findById(id);
    }

    @Cacheable(value = "pins", key = "'board_' + #boardId")
    public List<Pin> getPinsByBoardId(Long boardId) {
        logger.debug("Загрузка пинов для доски с ID {}", boardId);
        return pinRepository.findByBoardId(boardId);
    }

    @CacheEvict(value = "pins", allEntries = true)
    public void deletePin(Long id) {
        logger.info("Удаление пина с ID {}", id);
        pinRepository.deleteById(id);
    }

    public PageResponse<Pin> getPins(String search, int pageNo, int pageSize) {
        pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        Page<Pin> page;
        if (search != null && !search.isEmpty()) {
            page = pinRepository.findByDescriptionContainingIgnoreCase(search, pageRequest);
        } else {
            page = pinRepository.findAll(pageRequest);
        }

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }


    public void calculateImageDimensions(Pin pin) {
        if (pin.getImageUrl() == null || pin.getImageUrl().isEmpty()) return;
        try {
            // пытаемся прочитать локальный файл
            String filename = fileStorageService.getFilenameFromUrl(pin.getImageUrl());
            java.nio.file.Path p = fileStorageService.getFileStoragePath().resolve(filename).normalize();
            BufferedImage img;
            if (p.toFile().exists()) {
                img = ImageIO.read(p.toFile());
            } else {
                // fallback: читаем по URL
                URL url = new URL(pin.getImageUrl());
                img = ImageIO.read(url);
            }
            if (img == null) {
                logger.warn("Не удалось загрузить картинку для пина {}: {}", pin.getId(), pin.getImageUrl());
                // fallback: пытаемся получить размеры через metadata
                try (InputStream metaIn = p.toFile().exists()
                        ? new FileInputStream(p.toFile())
                        : new URL(pin.getImageUrl()).openStream()) {
                    Metadata metadata = ImageMetadataReader.readMetadata(metaIn);
                    JpegDirectory jpegDir = metadata.getFirstDirectoryOfType(JpegDirectory.class);
                    if (jpegDir != null && jpegDir.containsTag(JpegDirectory.TAG_IMAGE_WIDTH)) {
                        int fw = jpegDir.getInt(JpegDirectory.TAG_IMAGE_WIDTH);
                        int fh = jpegDir.getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
                        pin.setImageWidth(fw);
                        pin.setImageHeight(fh);
                        return;
                    }
                } catch (Exception ex) {
                    logger.warn("Не удалось получить размеры через metadata для пина {}: {}", pin.getId(), ex.getMessage());
                }
                // задаём дефолтные размеры, чтобы не было null в БД
                pin.setImageWidth(null);
                pin.setImageHeight(null);
                return;
            }
            int w = img.getWidth();
            int h = img.getHeight();
            // пытаемся применить EXIF-ориентацию, но ошибки здесь не прерывают процесс
            try (InputStream metaIn = p.toFile().exists()
                    ? new java.io.FileInputStream(p.toFile())
                    : new URL(pin.getImageUrl()).openStream()) {
                Metadata metadata = ImageMetadataReader.readMetadata(metaIn);
                ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (dir != null && dir.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                    int orientation = dir.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                    if (orientation == 6 || orientation == 8) {
                        int tmp = w; w = h; h = tmp;
                    }
                }
            } catch (Exception ex) {
                logger.warn("Не удалось прочитать EXIF ориентацию для пина {}: {}", pin.getId(), ex.getMessage());
            }
            pin.setImageWidth(w);
            pin.setImageHeight(h);
        } catch (Exception e) {
            logger.warn("Не удалось вычислить размеры картинки для пина {}: {}", pin.getId(), e.getMessage());
            // при ошибке ставим нулевые размеры
            pin.setImageWidth(null);
            pin.setImageHeight(null);
        }
    }

    @CacheEvict(value = "pins", allEntries = true)
    public Pin createPin(PinRequest pinRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Pin pin = new Pin();
        pin.setTitle(pinRequest.getTitle());
        pin.setImageUrl(pinRequest.getImageUrl());
        pin.setDescription(pinRequest.getDescription());
        pin.setUser(user);
        pin.setRating(pinRequest.getRating());
        if (pinRequest.getBoardId() != null) {
            Board board = boardService.getBoardById(pinRequest.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Доска не найдена"));
            pin.setBoard(board);
        }

        // устанавливаем размеры изображения
        calculateImageDimensions(pin);

        // Инициализируем счётчик комментариев
        pin.setCommentsCount(0);
        Pin savedPin = pinRepository.save(pin);
        // Publish event for additional processing (e.g., high-rating board assignment)
        eventPublisher.publishEvent(new com.example.server.UsPinterest.event.PinCreatedEvent(savedPin));
        return savedPin;
    }

    @Cacheable(value = "pins", key = "'user_' + #username")
    public List<Pin> getPinsByUser(String username) {
        return pinRepository.findByUserUsername(username);
    }

    @Transactional
    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public Map<String, Object> likePin(Long pinId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        Optional<Like> likeOptional = likeRepository.findByPinAndUser(pin, user);
        Map<String, Object> responseMap = new HashMap<>();

        if (likeOptional.isPresent()) {
            responseMap.put("message", "Лайк уже существует");
            responseMap.put("liked", true);
            responseMap.put("likesCount", pin.getLikesCount());
            return responseMap;
        }

        Like like = new Like();
        like.setUser(user);
        like.setPin(pin);
        like.setCreatedAt(LocalDateTime.now());
        likeRepository.save(like);
        int totalLikes = likeRepository.countByPinId(pinId);
        pin.setLikesCount(totalLikes);
        pinRepository.save(pin);
        responseMap.put("message", "Лайк поставлен");
        responseMap.put("liked", true);
        responseMap.put("likesCount", totalLikes);
        return responseMap;
    }

    @Transactional
    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public Map<String, Object> unlikePin(Long pinId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        Optional<Like> likeOptional = likeRepository.findByPinAndUser(pin, user);
        Map<String, Object> responseMap = new HashMap<>();

        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
            int totalLikes = likeRepository.countByPinId(pinId);
            pin.setLikesCount(totalLikes);
            pinRepository.save(pin);
            responseMap.put("message", "Лайк удалён");
            responseMap.put("liked", false);
            responseMap.put("likesCount", totalLikes);
            return responseMap;
        }

        responseMap.put("message", "Лайк не найден");
        responseMap.put("liked", false);
        responseMap.put("likesCount", pin.getLikesCount());
        return responseMap;
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public String updatePinImageUrl(Pin pin) {
        if (pin == null || pin.getImageUrl() == null) {
            return null;
        }

        try {
            String imageUrl = pin.getImageUrl();
            String updatedImageUrl = fileStorageService.updateImageUrl(imageUrl);

            if (!imageUrl.equals(updatedImageUrl)) {
                pin.setImageUrl(updatedImageUrl);
                pinRepository.save(pin);
            }

            return updatedImageUrl;
        } catch (Exception e) {
            logger.error("Error updating pin image URL: {}", e.getMessage());
            return pin.getImageUrl();
        }
    }

    @Transactional
    public PinResponse convertToPinResponse(Pin pin, User currentUser) {
        // Базовое маппирование статических полей через MapStruct
        PinResponse response = pinStructMapper.toDto(pin);

        // Обновляем URL картинки
        if (pin.getImageUrl() != null && !pin.getImageUrl().isEmpty()) {
            response.setImageUrl(fileStorageService.updateImageUrl(pin.getImageUrl()));
        }

        // Подсчитываем и устанавливаем статистику
        response.setLikesCount(pin.getLikesCount() != null ? pin.getLikesCount() : 0);
        long commentCount = commentRepository.countByPinId(pin.getId());
        response.setCommentsCount((int) commentCount);

        // Проверяем, лайкнул ли текущий пользователь
        boolean isLiked = currentUser != null && pin.getLikes().stream()
                .anyMatch(like -> like.getUser() != null && currentUser.getId().equals(like.getUser().getId()));
        response.setIsLikedByCurrentUser(isLiked);

        // Устанавливаем соотношение сторон
        Integer w = pin.getImageWidth(), h = pin.getImageHeight();
        response.setAspectRatio(w != null && h != null && h > 0 ? w.doubleValue() / h : 1.0);

        // Обновляем URLы FullHD и миниатюр
        if (pin.getFullhdImageUrl() != null && !pin.getFullhdImageUrl().isEmpty()) {
            response.setFullhdImageUrl(fileStorageService.updateImageUrl(pin.getFullhdImageUrl()));
        }
        if (pin.getThumbnailImageUrl() != null && !pin.getThumbnailImageUrl().isEmpty()) {
            response.setThumbnailImageUrl(fileStorageService.updateImageUrl(pin.getThumbnailImageUrl()));
        }

        return response;
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursor_lt_' + #cursorId + '_' + #limit")
    public List<Pin> findPinsLessThan(Long cursorId, int limit) {
        return pinRepository.findByIdLessThanOrderByIdDesc(cursorId, PageRequest.of(0, limit));
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursor_gt_' + #cursorId + '_' + #limit")
    public List<Pin> findPinsGreaterThan(Long cursorId, int limit) {
        return pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, PageRequest.of(0, limit));
    }

    public long count() {
        return pinRepository.count();
    }

    @Transactional(readOnly = true)
    public Pin getPinWithLikesAndComments(Long pinId) {
        // Подгружаем пин вместе с лайками и комментариями через EntityGraph
        return pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден с id: " + pinId));
    }


    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursorPage_' + #cursor + '_' + #size + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public CursorPageResponse<PinResponse, String> getPinsCursor(String cursor, int size, String sortDirection) {
        // Декодируем курсор и определяем fetchSize, направление сортировки
        Long cursorId = paginationService.decodeCursor(cursor, Long.class);
        boolean isDesc = sortDirection == null || !sortDirection.equalsIgnoreCase("asc");
        int fetchSize = size > 0 ? size + 1 : DEFAULT_PAGE_SIZE + 1;

        List<Pin> raw;
        if (isDesc) {
            if (cursorId == null) {
                raw = pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").descending())).getContent();
            } else {
                raw = pinRepository.findByIdLessThanOrderByIdDesc(cursorId, PageRequest.of(0, fetchSize));
            }
        } else {
            if (cursorId == null) {
                raw = pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").ascending())).getContent();
            } else {
                raw = pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, PageRequest.of(0, fetchSize));
            }
        }

        boolean hasNext = raw.size() > size;
        if (hasNext) {
            raw.remove(raw.size() - 1);
        }

        // Преобразуем в DTO
        // Текущий пользователь на уровне контроллера
        User currentUser = null;
        List<PinResponse> content = raw.stream()
                .map(pin -> convertToPinResponse(pin, currentUser))
                .collect(Collectors.toList());

        // Генерация курсоров
        String nextCursor = hasNext ? paginationService.encodeCursor(raw.get(raw.size() - 1).getId()) : null;
        boolean hasPrevious = cursorId != null;
        String prevCursor = hasPrevious ? paginationService.encodeCursor(cursorId) : null;

        // Общее число элементов
        long totalElements = pinRepository.count();

        return paginationService.createCursorPageResponse(
                content,
                nextCursor,
                prevCursor,
                hasNext,
                hasPrevious,
                size,
                totalElements
        );
    }

    // Новый метод: получение FullHD изображений пинов с курсорной пагинацией
    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursorFullhd_' + #cursor + '_' + #size + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public CursorPageResponse<PinFullHdResponse, String> getPinsFullhdCursor(String cursor, int size, String sortDirection) {
        Long cursorId = paginationService.decodeCursor(cursor, Long.class);
        boolean isDesc = sortDirection == null || !sortDirection.equalsIgnoreCase("asc");
        int fetchSize = size > 0 ? size + 1 : DEFAULT_PAGE_SIZE + 1;
        List<Pin> raw;
        if (isDesc) {
            if (cursorId == null) {
                raw = pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").descending())).getContent();
            } else {
                raw = pinRepository.findByIdLessThanOrderByIdDesc(cursorId, PageRequest.of(0, fetchSize));
            }
        } else {
            if (cursorId == null) {
                raw = pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").ascending())).getContent();
            } else {
                raw = pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, PageRequest.of(0, fetchSize));
            }
        }
        boolean hasNext = raw.size() > size;
        if (hasNext) raw.remove(raw.size() - 1);
        List<PinFullHdResponse> content = raw.stream()
                .map(pin -> {
                    // MapStruct-меппинг основного DTO
                    PinFullHdResponse dto = pinFullHdStructMapper.toDto(pin);
                    // Обновляем URL и aspectRatio
                    if (dto.getFullhdImageUrl() != null && !dto.getFullhdImageUrl().isEmpty()) {
                        dto.setFullhdImageUrl(fileStorageService.updateImageUrl(dto.getFullhdImageUrl()));
                    }
                    Integer w = pin.getImageWidth(), h = pin.getImageHeight();
                    dto.setAspectRatio(w != null && h != null && h > 0 ? w.doubleValue() / h : 1.0);
                    return dto;
                }).collect(Collectors.toList());
        String nextCursor = hasNext ? paginationService.encodeCursor(raw.get(raw.size() - 1).getId()) : null;
        boolean hasPrevious = cursorId != null;
        String prevCursor = hasPrevious ? paginationService.encodeCursor(cursorId) : null;
        long totalElements = pinRepository.count();
        return paginationService.createCursorPageResponse(
                content,
                nextCursor,
                prevCursor,
                hasNext,
                hasPrevious,
                size,
                totalElements
        );
    }

    // Новый метод: получение миниатюр WebP изображений пинов с курсорной пагинацией
    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursorThumbnail_' + #cursor + '_' + #size + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public CursorPageResponse<PinThumbnailResponse, String> getPinsThumbnailCursor(String cursor, int size, String sortDirection) {
        Long cursorId = paginationService.decodeCursor(cursor, Long.class);
        boolean isDesc = sortDirection == null || !sortDirection.equalsIgnoreCase("asc");
        int fetchSize = size > 0 ? size + 1 : DEFAULT_PAGE_SIZE + 1;
        List<Pin> raw;
        if (isDesc) {
            if (cursorId == null) {
                raw = pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").descending())).getContent();
            } else {
                raw = pinRepository.findByIdLessThanOrderByIdDesc(cursorId, PageRequest.of(0, fetchSize));
            }
        } else {
            if (cursorId == null) {
                raw = pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").ascending())).getContent();
            } else {
                raw = pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, PageRequest.of(0, fetchSize));
            }
        }
        boolean hasNext = raw.size() > size;
        if (hasNext) raw.remove(raw.size() - 1);
        List<PinThumbnailResponse> content = raw.stream()
                .map(pin -> {
                    PinThumbnailResponse dto = pinThumbnailStructMapper.toDto(pin);
                    if (dto.getThumbnailImageUrl() != null && !dto.getThumbnailImageUrl().isEmpty()) {
                        dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(dto.getThumbnailImageUrl()));
                    }
                    Integer w2 = pin.getImageWidth(), h2 = pin.getImageHeight();
                    dto.setAspectRatio(w2 != null && h2 != null && h2 > 0 ? w2.doubleValue() / h2 : 1.0);
                    return dto;
                }).collect(Collectors.toList());
        String nextCursor2 = hasNext ? paginationService.encodeCursor(raw.get(raw.size() - 1).getId()) : null;
        boolean hasPrev2 = cursorId != null;
        String prevCursor2 = hasPrev2 ? paginationService.encodeCursor(cursorId) : null;
        long totalEl = pinRepository.count();
        return paginationService.createCursorPageResponse(content, nextCursor2, prevCursor2, hasNext, hasPrev2, size, totalEl);
    }

    @Transactional
    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public void recalcImageDimensionsForAllPins() {
        List<Pin> pins = pinRepository.findAll();
        for (Pin pin : pins) {
            if (pin.getImageUrl() != null && !pin.getImageUrl().isEmpty()) {
                // используем единый метод вычисления размеров
                calculateImageDimensions(pin);
                pinRepository.save(pin);
            }
        }
    }

    @Transactional
    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public void generateImageVariantsForAllPins() {
        List<Pin> pins = pinRepository.findAll();
        for (Pin pin : pins) {
            try {
                String originalUrl = pin.getImageUrl();
                if (originalUrl == null || originalUrl.isEmpty()) {
                    continue;
                }
                String filename = fileStorageService.getFilenameFromUrl(originalUrl);
                Path sourcePath = fileStorageService.getFileStoragePath().resolve(filename).normalize();
                if (!Files.exists(sourcePath)) {
                    logger.warn("Source file not found for pin {}: {}", pin.getId(), sourcePath);
                    continue;
                }
                BufferedImage originalImg = ImageIO.read(sourcePath.toFile());
                if (originalImg == null) {
                    logger.warn("Failed to read image for pin {}: {}", pin.getId(), sourcePath);
                    continue;
                }
                String baseName = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;

                // FullHD
                Path fullhdDir = fileStorageService.getFullhdImagesLocation();
                BufferedImage fullhdImg = Thumbnails.of(originalImg)
                        .size(fileStorageService.getFullhdMaxWidth(), fileStorageService.getFullhdMaxHeight())
                        .outputFormat("webp")
                        .asBufferedImage();
                String fullhdFilename = baseName + ".webp";
                Path fullhdPath = fullhdDir.resolve(fullhdFilename);
                ImageIO.write(fullhdImg, "webp", fullhdPath.toFile());
                String fullhdUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/").path(fileStorageService.getFullhdImagesDir()).path("/").path(fullhdFilename).toUriString();

                // Thumbnail
                Path thumbDir = fileStorageService.getThumbnailImagesLocation();
                BufferedImage thumbImg = Thumbnails.of(originalImg)
                        .size(fileStorageService.getThumbnailMaxWidth(), fileStorageService.getThumbnailMaxHeight())
                        .outputFormat("webp")
                        .asBufferedImage();
                String thumbFilename = baseName + ".webp";
                Path thumbPath = thumbDir.resolve(thumbFilename);
                ImageIO.write(thumbImg, "webp", thumbPath.toFile());
                String thumbUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/").path(fileStorageService.getThumbnailImagesDir()).path("/").path(thumbFilename).toUriString();

                // Сохраняем в сущность
                pin.setFullhdImageUrl(fullhdUrl);
                pin.setFullhdWidth(fullhdImg.getWidth());
                pin.setFullhdHeight(fullhdImg.getHeight());
                pin.setThumbnailImageUrl(thumbUrl);
                pin.setThumbnailWidth(thumbImg.getWidth());
                pin.setThumbnailHeight(thumbImg.getHeight());

                pinRepository.save(pin);
            } catch (Exception e) {
                logger.error("Error generating image variants for pin {}: {}", pin.getId(), e.getMessage(), e);
            }
        }
    }
}