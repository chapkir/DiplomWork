package com.example.server.UsPinterest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-images-dir:profile-images}")
    private String profileImagesDir;

    @Value("${api.current-version:v1}")
    private String currentApiVersion;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/profile").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        logger.info("Configuring resource handler for uploads. Path: {}", uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600);

        String profileImagesPath = Paths.get(uploadDir, profileImagesDir).toAbsolutePath().normalize().toString();
        logger.info("Configuring resource handler for profile images. Path: {}", profileImagesPath);

        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations("file:" + profileImagesPath + "/")
                .setCachePeriod(3600);


        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .setCachePeriod(3600);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8081",
                        "http://127.0.0.1:8081",
                        "capacitor://localhost",
                        "ionic://localhost",
                        "http://localhost",
                        "https://localhost",
                        "*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(false)
                .maxAge(3600);
    }


    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {


        logger.info("API versioning set to version: {}", currentApiVersion);

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        converters.add(0, converter); // Add at first position to ensure it's used
        logger.info("Configured custom Jackson message converter");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }

    @Bean
    public OncePerRequestFilter headerFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");

                if (request.getRequestURI().contains("/api/")) {
                    response.setHeader("X-API-Version", currentApiVersion);
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}