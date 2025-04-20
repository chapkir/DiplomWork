package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PageResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.server.UsPinterest.service.FileStorageService;

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

    @Cacheable(value = "pins", key = "#id")
    public Optional<Pin> getPinById(Long id) {
        logger.debug("Загрузка пина с ID {} из базы данных", id);
        return pinRepository.findByIdWithLikesAndComments(id);
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

    @CacheEvict(value = "pins", allEntries = true)
    public Pin createPin(PinRequest pinRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Pin pin = new Pin();
        pin.setImageUrl(pinRequest.getImageUrl());
        pin.setDescription(pinRequest.getDescription());
        pin.setUser(user);

        if (pinRequest.getBoardId() != null) {
            Board board = boardService.getBoardById(pinRequest.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Доска не найдена"));
            pin.setBoard(board);
        }

        return pinRepository.save(pin);
    }

    @Cacheable(value = "pins", key = "'user_' + #username")
    public List<Pin> getPinsByUser(String username) {
        return pinRepository.findByUserUsername(username);
    }

    @Transactional
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
            return responseMap;
        }

        Like like = new Like();
        like.setUser(user);
        like.setPin(pin);
        like.setCreatedAt(LocalDateTime.now());
        likeRepository.save(like);
        responseMap.put("message", "Лайк поставлен");
        responseMap.put("liked", true);
        return responseMap;
    }

    @Transactional
    public Map<String, Object> unlikePin(Long pinId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        Optional<Like> likeOptional = likeRepository.findByPinAndUser(pin, user);
        Map<String, Object> responseMap = new HashMap<>();

        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
            responseMap.put("message", "Лайк удалён");
            responseMap.put("liked", false);
            return responseMap;
        }

        responseMap.put("message", "Лайк не найден");
        responseMap.put("liked", false);
        return responseMap;
    }

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

    @Transactional(readOnly = true)
    public PinResponse convertToPinResponse(Pin pin, User currentUser) {
        try {
            PinResponse response = new PinResponse();
            response.setId(pin.getId());

            try {
                String imageUrl = pin.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    imageUrl = fileStorageService.updateImageUrl(imageUrl);
                }
                response.setImageUrl(imageUrl);
            } catch (Exception e) {
                logger.error("Error updating image URL for pin {}: {}", pin.getId(), e.getMessage());
                response.setImageUrl(pin.getImageUrl());
            }

            response.setDescription(pin.getDescription());

            if (pin.getBoard() != null) {
                response.setBoardId(pin.getBoard().getId());
                response.setBoardTitle(pin.getBoard().getTitle());
            }

            if (pin.getUser() != null) {
                response.setUserId(pin.getUser().getId());
                response.setUsername(pin.getUser().getUsername());

                String profileImageUrl = pin.getUser().getProfileImageUrl();
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    profileImageUrl = fileStorageService.updateImageUrl(profileImageUrl);
                }
                response.setUserProfileImageUrl(profileImageUrl);
            }

            response.setCreatedAt(pin.getCreatedAt());
            response.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);

            boolean isLiked = false;
            if (currentUser != null && pin.getLikes() != null) {
                isLiked = pin.getLikes().stream()
                        .filter(like -> like.getUser() != null)
                        .anyMatch(like -> currentUser.getId().equals(like.getUser().getId()));
            }
            response.setIsLikedByCurrentUser(isLiked);

            return response;
        } catch (Exception e) {
            logger.error("Error converting Pin to PinResponse: {}", e.getMessage());
            // Create a minimal response to avoid null returns
            PinResponse fallbackResponse = new PinResponse();
            if (pin != null) {
                fallbackResponse.setId(pin.getId());
                fallbackResponse.setDescription(pin.getDescription());
                fallbackResponse.setImageUrl(pin.getImageUrl());
                fallbackResponse.setCreatedAt(pin.getCreatedAt());
            }
            return fallbackResponse;
        }
    }

    @Cacheable(value = "pins", key = "'cursor_lt_' + #cursorId + '_' + #limit")
    public List<Pin> findPinsLessThan(Long cursorId, int limit) {
        return pinRepository.findByIdLessThanWithLikesOrderByIdDesc(cursorId, PageRequest.of(0, limit));
    }

    @Cacheable(value = "pins", key = "'cursor_gt_' + #cursorId + '_' + #limit")
    public List<Pin> findPinsGreaterThan(Long cursorId, int limit) {
        return pinRepository.findByIdGreaterThanWithLikesOrderByIdAsc(cursorId, PageRequest.of(0, limit));
    }

    public long count() {
        return pinRepository.count();
    }

    @Transactional(readOnly = true)
    public Pin getPinWithLikesAndComments(Long pinId) {

        Pin pinWithLikes = pinRepository.findByIdWithLikesAndComments(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден с id: " + pinId));


        pinRepository.findByIdWithComments(pinId).ifPresent(pinWithComments -> {
            pinWithLikes.setComments(pinWithComments.getComments());
        });

        return pinWithLikes;
    }
}