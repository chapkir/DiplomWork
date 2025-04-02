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


@Service
public class PaginationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;


    public Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        size = validatePageSize(size);
        Sort sort = createSort(sortBy, sortDirection);
        return PageRequest.of(page, size, sort);
    }

    public Pageable createPageable(int page, int size) {
        size = validatePageSize(size);
        return PageRequest.of(page, size);
    }


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


    private int validatePageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        } else if (size > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return size;
    }


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
                throw new IllegalArgumentException(" " + cursorType.getName());
            }
        } catch (Exception e) {
            return null;
        }
    }


    public String encodeCursor(Object value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.toString().getBytes());
    }

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