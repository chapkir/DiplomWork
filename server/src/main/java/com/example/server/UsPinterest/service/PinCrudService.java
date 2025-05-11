package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PinCrudService {

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
    private ApplicationEventPublisher eventPublisher;

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public Pin createPin(PinRequest pinRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = new Pin();
        pin.setTitle(pinRequest.getTitle());
        pin.setImageUrl(pinRequest.getImageUrl());
        pin.setDescription(pinRequest.getDescription());
        pin.setUser(user);
        pin.setRating(pinRequest.getRating());
        if (pinRequest.getBoardId() != null) {
            Board board = boardService.getBoardById(pinRequest.getBoardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Доска не найдена"));
            pin.setBoard(board);
        }
        calculateImageDimensions(pin);
        pin.setCommentsCount(0);
        Pin saved = pinRepository.save(pin);
        eventPublisher.publishEvent(new com.example.server.UsPinterest.event.PinCreatedEvent(saved));
        return saved;
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public void deletePin(Long id) {
        pinRepository.deleteById(id);
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public Map<String, Object> likePin(Long pinId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));
        Optional<Like> exists = likeRepository.findByPinAndUser(pin, user);
        Map<String, Object> response = new HashMap<>();
        if (exists.isPresent()) {
            response.put("message", "Лайк уже существует");
            response.put("liked", true);
            response.put("likesCount", pin.getLikesCount());
            return response;
        }
        Like like = new Like();
        like.setUser(user);
        like.setPin(pin);
        like.setCreatedAt(LocalDateTime.now());
        likeRepository.save(like);
        int total = likeRepository.countByPinId(pinId);
        pin.setLikesCount(total);
        pinRepository.save(pin);
        response.put("message", "Лайк поставлен");
        response.put("liked", true);
        response.put("likesCount", total);
        return response;
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public Map<String, Object> unlikePin(Long pinId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));
        Map<String, Object> response = new HashMap<>();
        likeRepository.findByPinAndUser(pin, user).ifPresent(like -> {
            likeRepository.delete(like);
        });
        int total = likeRepository.countByPinId(pinId);
        pin.setLikesCount(total);
        pinRepository.save(pin);
        response.put("message", "Лайк удалён");
        response.put("liked", false);
        response.put("likesCount", total);
        return response;
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public String updatePinImageUrl(Pin pin) {
        if (pin == null || pin.getImageUrl() == null) return null;
        String updated = fileStorageService.updateImageUrl(pin.getImageUrl());
        if (!pin.getImageUrl().equals(updated)) {
            pin.setImageUrl(updated);
            pinRepository.save(pin);
        }
        return updated;
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public void recalcImageDimensionsForAllPins() {
        pinRepository.findAll().forEach(pin -> {
            if (pin.getImageUrl() != null) {
                calculateImageDimensions(pin);
                pinRepository.save(pin);
            }
        });
    }

    @CacheEvict(value = {"pins", "search"}, allEntries = true)
    public void generateImageVariantsForAllPins() {
        pinRepository.findAll().forEach(pin -> {
            // reuse existing logic in PinService or FileStorageService
            // or call existing generateImageVariantsForAllPins
        });
    }

    private void calculateImageDimensions(Pin pin) {
        // delegate to existing method in PinService or FileStorageService
        new PinService().calculateImageDimensions(pin);
    }
}