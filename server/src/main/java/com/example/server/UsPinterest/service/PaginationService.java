package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * Сервис для работы с пагинацией
 */
@Service
public class PaginationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Создает объект Pageable для пагинации
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @param sortBy поле для сортировки
     * @param sortDirection направление сортировки (asc/desc)
     * @return объект Pageable для пагинации
     */
    public Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        int pageSize = Math.min(validatePageSize(size), MAX_PAGE_SIZE);
        Sort sort = createSort(sortBy, sortDirection);
        return PageRequest.of(Math.max(0, page), pageSize, sort);
    }

    /**
     * Создает объект Pageable для пагинации с сортировкой по умолчанию
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return объект Pageable для пагинации
     */
    public Pageable createPageable(int page, int size) {
        int pageSize = Math.min(validatePageSize(size), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(0, page), pageSize);
    }

    /**
     * Создает объект Sort для сортировки
     *
     * @param sortBy поле для сортировки
     * @param sortDirection направление сортировки (asc/desc)
     * @return объект Sort для сортировки
     */
    public Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.unsorted();
        }

        return Sort.by(direction, sortBy);
    }

    /**
     * Проверяет и корректирует размер страницы
     *
     * @param size размер страницы
     * @return скорректированный размер страницы
     */
    private int validatePageSize(int size) {
        return size <= 0 ? DEFAULT_PAGE_SIZE : size;
    }

    /**
     * Создает объект PageResponse из Page
     *
     * @param page объект Page с данными
     * @param <T> тип данных
     * @return объект PageResponse
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
     * Создает объект PageResponse из Page с преобразованием элементов
     *
     * @param page объект Page с данными
     * @param converter функция для преобразования элементов
     * @param <T> тип исходных данных
     * @param <R> тип данных после преобразования
     * @return объект PageResponse
     */
    public <T, R> PageResponse<R> createPageResponse(Page<T> page, Function<T, R> converter) {
        List<R> convertedContent = page.getContent().stream()
                .map(converter)
                .toList();

        return new PageResponse<>(
                convertedContent,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}