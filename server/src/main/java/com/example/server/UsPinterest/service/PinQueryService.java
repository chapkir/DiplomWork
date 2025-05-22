package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.PinFullHdResponse;
import com.example.server.UsPinterest.dto.PinThumbnailResponse;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.server.UsPinterest.dto.mapper.PinStructMapper;
import com.example.server.UsPinterest.dto.mapper.PinFullHdStructMapper;
import com.example.server.UsPinterest.dto.mapper.PinThumbnailStructMapper;
import com.example.server.UsPinterest.service.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import com.example.server.UsPinterest.repository.PictureRepository;
import com.example.server.UsPinterest.repository.LocationRepository;
import com.example.server.UsPinterest.model.Location;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PinRepository pinRepository;

    private final CommentRepository commentRepository;

    private final PaginationService paginationService;

    private final FileStorageService fileStorageService;

    private final PinStructMapper pinStructMapper;

    private final PinFullHdStructMapper pinFullHdStructMapper;

    private final PinThumbnailStructMapper pinThumbnailStructMapper;

    private final PinService pinService;

    private final PictureRepository pictureRepository;

    private final LocationRepository locationRepository;

    @Cacheable(value = "pins", key = "#id")
    public Optional<Pin> getPinById(Long id) {
        return pinRepository.findById(id);
    }

    @Cacheable(value = "pins", key = "'board_' + #boardId")
    public List<Pin> getPinsByBoardId(Long boardId) {
        return pinRepository.findByBoardId(boardId);
    }

    public PageResponse<Pin> getPins(String search, int page, int size) {
        int pageSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        var pageReq = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        var result = (search != null && !search.isEmpty())
                ? pinRepository.findByDescriptionContainingIgnoreCase(search, pageReq)
                : pinRepository.findAll(pageReq);
        return paginationService.createPageResponse(result, p -> p);
    }

    @Transactional(readOnly = true)
    public Pin getPinWithLikesAndComments(Long id) {
        return pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден с id: " + id));
    }

    public PinResponse convertToPinResponse(Pin pin, User currentUser) {
        // Delegate mapping of static and computed fields (URL, counts, aspect ratio) to MapStruct decorator
        PinResponse dto = pinStructMapper.toDto(pin);
        // Set dynamic user-specific field
        boolean isLiked = currentUser != null && pin.getLikes().stream()
                .anyMatch(like -> like.getUser() != null && currentUser.getId().equals(like.getUser().getId()));
        dto.setIsLikedByCurrentUser(isLiked);
        return dto;
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursor_lt_' + #cursorId + '_' + #limit")
    public List<Pin> findPinsLessThan(Long cursorId, int limit) {
        return pinRepository.findByIdLessThanOrderByIdDesc(cursorId, PageRequest.of(0, limit));
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursor_gt_' + #cursorId + '_' + #limit")
    public List<Pin> findPinsGreaterThan(Long cursorId, int limit) {
        return pinRepository.findByIdGreaterThanOrderByIdAsc(cursorId, PageRequest.of(0, limit));
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursorPage_' + #cursor + '_' + #size + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public CursorPageResponse<PinResponse, String> getPinsCursor(String cursor, int size, String sortDirection) {
        Long cursorId = paginationService.decodeCursor(cursor, Long.class);
        boolean isDesc = sortDirection == null || !sortDirection.equalsIgnoreCase("asc");
        int fetchSize = size > 0 ? size + 1 : DEFAULT_PAGE_SIZE + 1;
        List<Pin> raw = isDesc
                ? (cursorId == null
                ? pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").descending())).getContent()
                : findPinsLessThan(cursorId, fetchSize))
                : (cursorId == null
                ? pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").ascending())).getContent()
                : findPinsGreaterThan(cursorId, fetchSize));
        boolean hasNext = raw.size() > size;
        List<Pin> pageList = new ArrayList<>(raw);
        if (hasNext) pageList.remove(pageList.size() - 1);
        User currentUser = null;
        List<PinResponse> content = pageList.stream().map(p -> convertToPinResponse(p, currentUser)).collect(Collectors.toList());
        // Вычисляем необработанные значения курсоров
        Long nextCursorValue = hasNext ? pageList.get(pageList.size() - 1).getId() : null;
        boolean hasPrev = cursorId != null;
        Long prevCursorValue = hasPrev ? cursorId : null;
        long total = pinRepository.count();
        // Передаём необработанные курсоры, OpenApiConfig закодирует их в createCursorPageResponse
        return paginationService.createCursorPageResponse(content, nextCursorValue, prevCursorValue, hasNext, hasPrev, size, total);
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursorFullhd_' + #cursor + '_' + #size + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public CursorPageResponse<PinFullHdResponse, String> getPinsFullhdCursor(String cursor, int size, String sortDirection) {
        Long cursorId = paginationService.decodeCursor(cursor, Long.class);
        boolean isDesc = sortDirection == null || !sortDirection.equalsIgnoreCase("asc");
        int fetchSize = size > 0 ? size + 1 : DEFAULT_PAGE_SIZE + 1;
        List<Pin> raw = isDesc
                ? (cursorId == null
                ? pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").descending())).getContent()
                : findPinsLessThan(cursorId, fetchSize))
                : (cursorId == null
                ? pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").ascending())).getContent()
                : findPinsGreaterThan(cursorId, fetchSize));
        boolean hasNext = raw.size() > size;
        List<Pin> pageListHd = new ArrayList<>(raw);
        if (hasNext) pageListHd.remove(pageListHd.size() - 1);
        List<PinFullHdResponse> content = raw.stream()
                .map(pin -> {
                    PinFullHdResponse dto = pinFullHdStructMapper.toDto(pin);
                    if (dto.getFullhdImageUrl() != null && !dto.getFullhdImageUrl().isEmpty()) {
                        dto.setFullhdImageUrl(fileStorageService.updateImageUrl(dto.getFullhdImageUrl()));
                    }
                    Integer w = pin.getImageWidth(), h = pin.getImageHeight();
                    dto.setAspectRatio(w != null && h != null && h > 0 ? w.doubleValue() / h : 1.0);
                    return dto;
                }).collect(Collectors.toList());
        // Вычисляем необработанные значения курсоров
        Long nextCursorValueHd = hasNext ? pageListHd.get(pageListHd.size() - 1).getId() : null;
        boolean hasPrevHd = cursorId != null;
        Long prevCursorValueHd = hasPrevHd ? cursorId : null;
        long totalHd = pinRepository.count();
        // Передаём необработанные курсоры, OpenApiConfig закодирует их в createCursorPageResponse
        return paginationService.createCursorPageResponse(content, nextCursorValueHd, prevCursorValueHd, hasNext, hasPrevHd, size, totalHd);
    }

    @Cacheable(cacheManager = "extendedPinCacheManager", value = "extended_pins", key = "'cursorThumbnail_' + #cursor + '_' + #size + '_' + #sortDirection")
    @Transactional(readOnly = true)
    public CursorPageResponse<PinThumbnailResponse, String> getPinsThumbnailCursor(String cursor, int size, String sortDirection) {
        Long cursorId = paginationService.decodeCursor(cursor, Long.class);
        boolean isDesc = sortDirection == null || !sortDirection.equalsIgnoreCase("asc");
        int fetchSize = size > 0 ? size + 1 : DEFAULT_PAGE_SIZE + 1;
        List<Pin> raw = isDesc
                ? (cursorId == null
                ? pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").descending())).getContent()
                : findPinsLessThan(cursorId, fetchSize))
                : (cursorId == null
                ? pinRepository.findAll(PageRequest.of(0, fetchSize, Sort.by("id").ascending())).getContent()
                : findPinsGreaterThan(cursorId, fetchSize));
        boolean hasNext = raw.size() > size;
        List<Pin> pageListThumb = new ArrayList<>(raw);
        if (hasNext) pageListThumb.remove(pageListThumb.size() - 1);
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
        // Вычисляем необработанные значения курсоров
        Long nextCursorValueThumb = hasNext ? pageListThumb.get(pageListThumb.size() - 1).getId() : null;
        boolean hasPrevThumb = cursorId != null;
        Long prevCursorValueThumb = hasPrevThumb ? cursorId : null;
        long totalThumb = pinRepository.count();
        // Передаём необработанные курсоры, OpenApiConfig закодирует их в createCursorPageResponse
        return paginationService.createCursorPageResponse(content, nextCursorValueThumb, prevCursorValueThumb, hasNext, hasPrevThumb, size, totalThumb);
    }

    public void calculateImageDimensions(Pin pin) {
        pinService.calculateImageDimensions(pin);
    }

    /**
     * Обогащает PinResponse миниатюрой и данными локации
     */
    public PinResponse enrichPinResponse(PinResponse dto) {
        pictureRepository.findByPinId(dto.getId()).ifPresent(picture -> {
            String thumb1 = picture.getThumbnailImageUrl1();
            if (thumb1 != null && !thumb1.isEmpty()) {
                dto.setThumbnailImageUrl(fileStorageService.updateImageUrl(thumb1));
            }
            dto.setThumbnailWidth(picture.getThumbnailWidth());
            dto.setThumbnailHeight(picture.getThumbnailHeight());
        });
        List<Location> locs = locationRepository.findByPinId(dto.getId());
        if (!locs.isEmpty()) {
            Location loc = locs.get(0);
            dto.setLatitude(loc.getLatitude());
            dto.setLongitude(loc.getLongitude());
            dto.setAddress(loc.getAddress());
            dto.setPlaceName(loc.getNameplace());
        }
        return dto;
    }
}