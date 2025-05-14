package com.example.server.UsPinterest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import com.example.server.UsPinterest.dto.NotificationResponse;

@Service
public class SseService {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).remove(emitter));
        emitter.onTimeout(() -> emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("connected"));
        } catch (Exception e) {
            // Игнорируем ошибку отправки начального события
        }

        return emitter;
    }

    public void sendEvent(Long userId, NotificationResponse notification) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("notification").data(notification));
                } catch (Exception e) {
                    emitter.complete();
                    userEmitters.remove(emitter);
                }
            }
        }
    }
} 