package com.example.server.UsPinterest.config;

import com.example.server.UsPinterest.model.Board;
import com.example.server.UsPinterest.repository.BoardRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private BoardRepository boardRepository;

    @PostConstruct
    public void initBoards() {
        String[] titles = {"Высокий рейтинг", "Достопримечательности", "Парки", "Места для фото", "Кофейни"};
        for (String title : titles) {
            if (!boardRepository.existsByTitle(title)) {
                Board board = new Board();
                board.setTitle(title);
                board.setDescription("");
                boardRepository.save(board);
            }
        }
    }
}