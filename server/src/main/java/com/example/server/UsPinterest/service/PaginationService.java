package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пагинацией
 */
@Service
public class PaginationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Creates a Pageable object with sorting for paginated queries
     *
     * @param page          Page number (0-based)
     * @param size          Page size
     * @param sortBy        Field to sort by
     * @param sortDirection Sort direction (asc/desc)
     * @return Pageable object with the specified parameters
     */
    public Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        size = validatePageSize(size);
        Sort sort = createSort(sortBy, sortDirection);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates a Pageable object without sorting
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @return Pageable object with the specified parameters
     */
    public Pageable createPageable(int page, int size) {
        size = validatePageSize(size);
        return PageRequest.of(page, size);
    }

    /**
     * Creates a Sort object based on the specified parameters
     *
     * @param sortBy        Field to sort by
     * @param sortDirection Sort direction (asc/desc)
     * @return Sort object with the specified parameters
     */
    public Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";
        }

        return Sort.by(direction, sortBy);
    }

    /**
     * Validates and adjusts the page size
     *
     * @param size Requested page size
     * @return Adjusted page size
     */
    private int validatePageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        } else if (size > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return size;
    }

    /**
     * Creates a PageResponse from a Page object
     *
     * @param page Spring Data Page object
     * @param <T>  Type of the content
     * @return PageResponse with content and pagination info
     */
    public <T> PageResponse<T> createPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Creates a PageResponse from a Page object with mapping function
     *
     * @param page      Spring Data Page object
     * @param converter Function to convert elements
     * @param <T>       Type of source elements
     * @param <R>       Type of target elements
     * @return PageResponse with converted content and pagination info
     */
    public <T, R> PageResponse<R> createPageResponse(Page<T> page, Function<T, R> converter) {
        List<R> content = page.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Creates a cursor-based pagination request
     *
     * @param cursor     Base64 encoded cursor
     * @param size       Items per page
     * @param ascending  Sort direction
     * @return Decoded cursor information
     */
    public <T> T decodeCursor(String cursor, Class<T> cursorType) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(cursor);
            String decodedString = new String(decoded);

            if (cursorType == Long.class) {
                return cursorType.cast(Long.parseLong(decodedString));
            } else if (cursorType == String.class) {
                return cursorType.cast(decodedString);
            } else {
                throw new IllegalArgumentException("Unsupported cursor type: " + cursorType.getName());
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Encodes a cursor value
     *
     * @param value Value to encode
     * @return Base64 encoded cursor
     */
    public String encodeCursor(Object value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.toString().getBytes());
    }

    /**
     * Creates a cursor-based page response
     *
     * @param content Content list
     * @param cursor Current cursor value
     * @param nextCursorValue Value for the next cursor
     * @param prevCursorValue Value for the previous cursor
     * @param hasNext Whether there are more items after
     * @param hasPrevious Whether there are more items before
     * @param size Requested page size
     * @param totalElements Total elements count
     * @return Cursor-based page response
     */
    public <T, C> CursorPageResponse<T, String> createCursorPageResponse(
            List<T> content,
            C nextCursorValue,
            C prevCursorValue,
            boolean hasNext,
            boolean hasPrevious,
            int size,
            long totalElements) {

        String nextCursor = hasNext ? encodeCursor(nextCursorValue) : null;
        String prevCursor = hasPrevious ? encodeCursor(prevCursorValue) : null;

        return new CursorPageResponse<>(
                content,
                nextCursor,
                prevCursor,
                hasNext,
                hasPrevious,
                size,
                totalElements
        );
    }
}