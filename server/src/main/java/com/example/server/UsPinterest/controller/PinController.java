package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.CommentRequest;
import com.example.server.UsPinterest.dto.CommentResponse;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

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
            pr.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);

            pr.setComments(
                    pin.getComments().stream().map(comment -> {
                        CommentResponse cr = new CommentResponse();
                        cr.setId(comment.getId());
                        cr.setText(comment.getText());
                        cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                        return cr;
                    }).collect(Collectors.toList())
            );
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

    @PostMapping("/{pinId}/likes")
    public ResponseEntity<?> likePin(@PathVariable Long pinId, Authentication authentication) {
        System.out.println("Received like request for pin: " + pinId + " from user: " + authentication.getName());

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(pinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            Optional<Like> likeOptional = likeRepository.findByPinAndUser(pin, user);
            Map<String, Object> responseMap = new HashMap<>();
            if (likeOptional.isPresent()) {
                likeRepository.delete(likeOptional.get());
                responseMap.put("message", "Лайк удалён");
                responseMap.put("liked", false);
                System.out.println("Like removed for pin: " + pinId + " by user: " + user.getUsername());
                return ResponseEntity.ok(responseMap);
            } else {
                Like like = new Like();
                like.setUser(user);
                like.setPin(pin);
                like.setCreatedAt(LocalDateTime.now());
                likeRepository.save(like);
                responseMap.put("message", "Лайк поставлен");
                responseMap.put("liked", true);
                System.out.println("Like added for pin: " + pinId + " by user: " + user.getUsername());
                return ResponseEntity.ok(responseMap);
            }
        } catch (Exception e) {
            System.err.println("Error processing like for pin: " + pinId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{pinId}/comments")
    public ResponseEntity<MessageResponse> addComment(@PathVariable Long pinId,
                                                      @RequestBody CommentRequest commentRequest,
                                                      Authentication authentication) {
        System.out.println("Received comment request for pin: " + pinId + " from user: " + authentication.getName());
        System.out.println("Comment text: " + commentRequest.getText());

        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            Pin pin = pinRepository.findById(pinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

            Comment comment = new Comment();
            comment.setText(commentRequest.getText());
            comment.setPin(pin);
            comment.setUser(user);
            comment.setCreatedAt(LocalDateTime.now());
            Comment savedComment = commentRepository.save(comment);

            System.out.println("Comment added successfully with ID: " + savedComment.getId());
            return ResponseEntity.ok(new MessageResponse("Комментарий добавлен"));
        } catch (Exception e) {
            System.err.println("Error adding comment for pin: " + pinId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{pinId}")
    public ResponseEntity<PinResponse> getPinById(@PathVariable Long pinId) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        PinResponse response = new PinResponse();
        response.setId(pin.getId());
        response.setImageUrl(pin.getImageUrl());
        response.setDescription(pin.getDescription());
        response.setLikesCount(pin.getLikes() != null ? pin.getLikes().size() : 0);
        response.setComments(
                pin.getComments().stream().map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                    return cr;
                }).collect(Collectors.toList())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pinId}/comments")
    public ResponseEntity<List<CommentResponse>> getPinComments(@PathVariable Long pinId) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Пин не найден"));

        List<CommentResponse> comments = pin.getComments().stream()
                .map(comment -> {
                    CommentResponse cr = new CommentResponse();
                    cr.setId(comment.getId());
                    cr.setText(comment.getText());
                    cr.setUsername(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                    return cr;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }
}