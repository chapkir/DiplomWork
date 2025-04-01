package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.HateoasResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@ControllerAdvice
public class AdviceController implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(AdviceController.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof HateoasResponse) {
            response.getHeaders().add("X-Response-Type", "hateoas");

            if (selectedContentType.includes(MediaType.APPLICATION_JSON)) {
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            }

            try {
                logger.debug("Processing HATEOAS response: {}",
                        objectMapper.writeValueAsString(((HateoasResponse<?>) body).getMeta()));
            } catch (JsonProcessingException e) {
                logger.warn("Could not log HATEOAS response information", e);
            }
        }

        return body;
    }
}