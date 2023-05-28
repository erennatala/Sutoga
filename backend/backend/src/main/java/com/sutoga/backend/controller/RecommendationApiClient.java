package com.sutoga.backend.controller;

import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class RecommendationApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RecommendationApiClient(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter());
        this.baseUrl = baseUrl;
    }

    public String getRecommendations(String userId) {
        String url = baseUrl + "/recommend";

        // Set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Set request body with user ID
        String requestBody = "{\"user_id\": \"" + userId + "\"}";

        // Create the HTTP entity with headers and body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make the HTTP POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            // Handle error cases
            return null;
        }
    }
}