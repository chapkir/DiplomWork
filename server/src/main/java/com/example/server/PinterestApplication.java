package com.example.server;

import com.example.server.UsPinterest.service.PinService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class PinterestApplication {
    public static void main(String[] args) {
        SpringApplication.run(PinterestApplication.class, args);
    }




} 