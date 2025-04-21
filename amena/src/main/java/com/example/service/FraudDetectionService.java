package com.example.amena.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class FraudDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "http://localhost:8000/predict";

    public String predictFraud(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String requestBody = "{\"text\":\"" + text + "\"}";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            // Send the POST request to the prediction API
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
            // Get the prediction label and score from the response
            Map<String, Object> prediction = (Map<String, Object>) response.getBody();
            String label = ((Map<String, Object>) prediction.get("prediction")).get(0).toString();
            Double score = (Double) ((Map<String, Object>) prediction.get("prediction")).get("score");

            // Determine if it's fraud based on label or score threshold
            if (label.equals("LABEL_1") && score > 0.5) {
                return "fraud";
            } else {
                return "not fraud";
            }
        } catch (Exception e) {
            logger.error("Error while calling the fraud prediction API", e);
            return "Error occurred while processing the request";
        }
    }
}
