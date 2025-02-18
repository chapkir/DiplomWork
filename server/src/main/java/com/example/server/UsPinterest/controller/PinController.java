package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.MessageResponse;
import com.example.server.UsPinterest.dto.PinRequest;
import com.example.server.UsPinterest.dto.PinResponse;
import com.example.server.UsPinterest.entity.Comment;
import com.example.server.UsPinterest.entity.Like;
import com.example.server.UsPinterest.exception.ResourceNotFoundException;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.repository.CommentRepository;
import com.example.server.UsPinterest.repository.LikeRepository;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.repository.UserRepository;
import com.example.server.UsPinterest.service.PinService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pins")
@CrossOrigin(origins = "*")
public class PinController {

    @Autowired
    private PinService pinService;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping
    public ResponseEntity<List<Pin>> getAllPins() {
        List<Pin> pins = pinRepository.findAll();
        return ResponseEntity.ok(pins);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PinResponse>> getAllPinResponses() {
        List<Pin> pins = pinRepository.findAll();
        List<PinResponse> responses = pins.stream().map(pin -> {
            PinResponse pr = new PinResponse();
            pr.setId(pin.getId());
            pr.setImageUrl(pin.getImageUrl());
            pr.setDescription(pin.getDescription());
            return pr;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Pin> createPin(@RequestBody PinRequest pinRequest, Authentication authentication) {
        String username = authentication.getName();
        Pin createdPin = pinService.createPin(pinRequest, username);
        return ResponseEntity.ok(createdPin);
    }

    @PostMapping("/{pinId}/like")
    public ResponseEntity<MessageResponse> likePin(@PathVariable Long pinId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        Optional<Like> likeOptional = likeRepository.findByPinAndUser(pin, user);
        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
            return ResponseEntity.ok(new MessageResponse("Лайк удалён"));
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setPin(pin);
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);
            return ResponseEntity.ok(new MessageResponse("Лайк поставлен"));
        }
    }

    @PostMapping("/{pinId}/comments")
    public ResponseEntity<MessageResponse> addComment(@PathVariable Long pinId,
                                                      @RequestBody CommentRequest commentRequest,
                                                      Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setPin(pin);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        return ResponseEntity.ok(new MessageResponse("Комментарий добавлен"));
    }
}