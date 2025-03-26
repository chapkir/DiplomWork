package com.example.server.UsPinterest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-images-dir:profile-images}")
    private String profileImagesDir;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/acme-manager").setViewName("forward:/acme-manager.html");
        logger.info("View controllers configured");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadAbsolutePath = uploadPath.toUri().toString();

        Path profileImagesPath = Paths.get(uploadDir, profileImagesDir).toAbsolutePath().normalize();
        String profileImagesAbsolutePath = profileImagesPath.toUri().toString();

        logger.info("Configuring file resources at: {}", uploadAbsolutePath);
        logger.info("Configuring profile images at: {}", profileImagesAbsolutePath);

        // Main uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolutePath);

        // Add explicit resource handler for ACME challenge files
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations(uploadAbsolutePath + ".well-known/");

        // Profile images directory
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(profileImagesAbsolutePath);

        // Static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Добавляем явный обработчик для acme-manager.html
        registry.addResourceHandler("/acme-manager.html")
                .addResourceLocations("classpath:/static/");

        logger.info("Resource handlers configured");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring global CORS settings");
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8081",
                        "http://192.168.205.109:8081",
                        "http://192.168.1.125:8081",
                        "http://192.168.1.181:8081",
                        "http://127.0.0.1:8081",
                        "capacitor://localhost",
                        "ionic://localhost",
                        "http://localhost",
                        "https://localhost",
                        "*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .maxAge(3600);
        logger.info("Global CORS configuration complete");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        logger.info("Multipart resolver configured");
        return resolver;
    }

    @Bean
    public OncePerRequestFilter headerFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                response.setHeader("Permissions-Policy", null);

                // Add CORS headers
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setHeader("Access-Control-Max-Age", "3600");

                filterChain.doFilter(request, response);
            }
        };
    }
}