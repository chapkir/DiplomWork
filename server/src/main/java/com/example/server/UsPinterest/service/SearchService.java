package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.mapper.PinMapper;
import com.example.server.UsPinterest.dto.mapper.UserMapper;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для поиска контента
 */
@Service
public class SearchService {

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaginationService paginationService;

    @Autowired
    private PinMapper pinMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * Поиск пинов по ключевому слову с пагинацией
     *
     * @param keyword ключевое слово для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @param sortBy поле для сортировки
     * @param sortDirection направление сортировки
     * @return результаты поиска с пагинацией
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "search", key = "'pins_' + #keyword + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
    public PageResponse<PinResponse> searchPins(String keyword, int page, int size,
                                                String sortBy, String sortDirection) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = paginationService.createPageable(page, size, sortBy, sortDirection);

        Page<Pin> pinsPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            pinsPage = pinRepository.findByDescriptionContainingIgnoreCase(keyword, pageable);
        } else {
            pinsPage = pinRepository.findAll(pageable);
        }

        return paginationService.createPageResponse(pinsPage, pin -> pinMapper.toDto(pin, currentUser));
    }

    /**
     * Поиск пользователей по имени с пагинацией
     *
     * @param username имя пользователя для поиска
     * @param page номер страницы
     * @param size размер страницы
     * @return результаты поиска с пагинацией
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "search", key = "'users_' + #username + '_' + #page + '_' + #size")
    public PageResponse<ProfileResponse> searchUsers(String username, int page, int size) {
        Pageable pageable = paginationService.createPageable(page, size);

        Page<User> usersPage;
        if (username != null && !username.trim().isEmpty()) {
            usersPage = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return paginationService.createPageResponse(usersPage, user -> userMapper.toProfileDto(user));
    }
}