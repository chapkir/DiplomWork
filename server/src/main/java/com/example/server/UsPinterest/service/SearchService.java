package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.dto.ProfileResponse;
import com.example.server.UsPinterest.dto.mapper.PinStructMapper;
import com.example.server.UsPinterest.dto.mapper.UserStructMapper;
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

@Service
public class SearchService {

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaginationService paginationService;

    @Autowired
    private PinStructMapper pinStructMapper;

    @Autowired
    private UserStructMapper userStructMapper;

    @Autowired
    private UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(value = "search", key = "'pins_' + #keyword + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
    public PageResponse<PinResponse> searchPins(String keyword, int page, int size,
                                                String sortBy, String sortDirection) {
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : "";

        Pageable pageable = paginationService.createPageable(page, size, sortBy, sortDirection);
        Page<Pin> pinsPage = pinRepository.findByDescriptionContainingIgnoreCase(searchKeyword, pageable);

        return paginationService.createPageResponse(pinsPage, pin -> pinStructMapper.toDto(pin));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "search", key = "'users_' + #username + '_' + #page + '_' + #size")
    public PageResponse<ProfileResponse> searchUsers(String username, int page, int size) {
        String searchUsername = (username != null && !username.trim().isEmpty()) ? username.trim() : "";

        Pageable pageable = paginationService.createPageable(page, size);
        Page<User> usersPage = userRepository.findByUsernameContainingIgnoreCase(searchUsername, pageable);

        return paginationService.createPageResponse(usersPage, user -> userStructMapper.toProfileDto(user));
    }
}