package com.example.server.UsPinterest.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Duration;


@Configuration
public class RestClientConfig {


    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "javascript"));
        supportedMediaTypes.add(new MediaType("text", "javascript"));
        supportedMediaTypes.add(new MediaType("application", "*+json"));
        converter.setSupportedMediaTypes(supportedMediaTypes);


        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);


        RestTemplate restTemplate = new RestTemplateBuilder()
                .messageConverters(Collections.singletonList(converter))
                .build();

        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }
}