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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PinRepository pinRepository;

    private final UserRepository userRepository;

    private final PaginationService paginationService;

    private final PinStructMapper pinStructMapper;

    private final UserStructMapper userStructMapper;

    private final UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(value = "search", key = "'pins_' + #keyword + '_' + #tags + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
    public PageResponse<PinResponse> searchPins(String keyword, List<String> tags, int page, int size,
                                                String sortBy, String sortDirection) {
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : "";

        Pageable pageable = paginationService.createPageable(page, size, sortBy, sortDirection);
        List<String> searchTags = (tags != null && !tags.isEmpty()) ? tags : Collections.emptyList();
        Page<Pin> pinsPage;
        if (!searchTags.isEmpty()) {
            pinsPage = pinRepository.searchByDescriptionOrTags(searchKeyword, searchTags, pageable);
        } else {
            pinsPage = pinRepository.findByDescriptionContainingIgnoreCase(searchKeyword, pageable);
        }

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