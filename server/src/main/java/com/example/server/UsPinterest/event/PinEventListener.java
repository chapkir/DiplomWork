package com.example.server.UsPinterest.event;

import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.model.Pin;
import com.example.server.UsPinterest.repository.PinRepository;
import com.example.server.UsPinterest.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PinEventListener {

    @Autowired
    private BoardService boardService;

    @Autowired
    private PinRepository pinRepository;

    @TransactionalEventListener
    public void handlePinCreated(PinCreatedEvent event) {
        Pin pin = event.getPin();
        Double rating = pin.getRating();
        if (rating != null && rating >= 4.0) {
            Board highBoard = boardService.getOrCreateByTitle("Высокий рейтинг");
            pin.setBoard(highBoard);
            pinRepository.save(pin);
        }
    }
}