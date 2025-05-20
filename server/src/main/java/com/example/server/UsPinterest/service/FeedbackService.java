package com.example.server.UsPinterest.service;

import com.example.server.UsPinterest.entity.Feedback;
import com.example.server.UsPinterest.model.User;
import com.example.server.UsPinterest.dto.FeedbackRequest;
import com.example.server.UsPinterest.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Feedback createFeedback(User user, FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setWhatLiked(request.getWhatLiked());
        feedback.setWhatDisliked(request.getWhatDisliked());
        feedback.setRecommendations(request.getRecommendations());
        return feedbackRepository.save(feedback);
    }
} 