package com.example.server.UsPinterest.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = this.getClass().getClassLoader()
                    .getResourceAsStream("spotsy-chapkir-firebase-adminsdk-fbsvc-80eaac74b9.json");
            if (serviceAccount == null) {
                throw new RuntimeException("Не найден файл настроек Firebase");
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось инициализировать Firebase", e);
        }
    }
} 