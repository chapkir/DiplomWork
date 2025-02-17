package com.example.diplomwork.backend.UsPinterest.service;

import com.UsPinterest.dto.PinRequest;
import com.UsPinterest.model.Board;
import com.UsPinterest.model.Pin;
import com.UsPinterest.model.User;
import com.UsPinterest.repository.PinRepository;
import com.UsPinterest.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PinService {
    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserRepository userRepository;

    public Pin createPin(Pin pin) {
        return pinRepository.save(pin);
    }

    public Optional<Pin> getPinById(Long id) {
        return pinRepository.findById(id);
    }

    public List<Pin> getPinsByBoardId(Long boardId) {
        return pinRepository.findByBoardId(boardId);
    }

    public List<Pin> searchPinsByDescription(String keyword) {
        return pinRepository.findByDescriptionContainingIgnoreCase(keyword);
    }

    public void deletePin(Long id) {
        pinRepository.deleteById(id);
    }

    public List<Pin> getPins(String search) {
        if (search != null && !search.isEmpty()) {
            return pinRepository.findByDescriptionContainingIgnoreCase(search);
        }
        return pinRepository.findAll();
    }

    public Pin createPin(PinRequest pinRequest) {
        Pin newPin = new Pin(pinRequest.getImageUrl(), pinRequest.getDescription());
        if (pinRequest.getBoardId() != null) {
            Board board = boardService.getBoardById(pinRequest.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Доска не найдена"));
            newPin.setBoard(board);
        }
        return pinRepository.save(newPin);
    }

    public Pin createPin(PinRequest pinRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Pin pin = new Pin();
        pin.setImageUrl(pinRequest.getImageUrl());
        pin.setDescription(pinRequest.getDescription());
        pin.setUser(user);
        return pinRepository.save(pin);
    }


    public List<Pin> getPinsByUser(String username) {
        return pinRepository.findByUserUsername(username);
    }
} 