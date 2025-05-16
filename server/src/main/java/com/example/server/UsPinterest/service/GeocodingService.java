package com.example.server.UsPinterest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String getPlaceName(double latitude, double longitude) {
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("format", "json")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("zoom", 16)
                    .queryParam("addressdetails", 0)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "DiplomWorkApp/1.0");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = response.getBody();
                if (root.has("display_name")) {
                    return root.get("display_name").asText();
                }
            }
        } catch (Exception e) {
            // логируем и возвращаем null
            System.err.println("Ошибка геокодирования: " + e.getMessage());
        }
        return null;
    }
}