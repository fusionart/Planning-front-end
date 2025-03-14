package com.monbat.services;

import com.monbat.models.dto.ReadinessByWeek;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

public class LoadReadinessByWeek {
    public static List<ReadinessByWeek> getReadinessByWeek() {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:8080/api/calculations/plan10s";

        try {
            ResponseEntity<List<ReadinessByWeek>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            // Log the error and return an empty list
            LoggerFactory.getLogger(LoadReadinessByWeek.class).error("Error fetching readiness data", e);
            return Collections.emptyList();
        }
    }
}
