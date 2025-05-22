package com.example.server.UsPinterest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "UsPinterest API",
        version = "v1",
        description = "Документация REST API сервера UsPinterest"
    )
)
public class OpenApiConfig {
} 