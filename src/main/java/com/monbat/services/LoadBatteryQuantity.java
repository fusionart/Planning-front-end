package com.monbat.services;

import com.monbat.models.entities.BatteryQuantity;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

public class LoadBatteryQuantity {
    public static List<BatteryQuantity> getBatteryQuantity(int location) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:8080/api/calculations/quantityByLocation";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
//                .queryParam("param2", material)
                .queryParam("param1", location);

        try {
            ResponseEntity<List<BatteryQuantity>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            // Log the error and return an empty list
            LoggerFactory.getLogger(LoadBatteryQuantity.class).error("Error fetching readiness data", e);
            return Collections.emptyList();
        }
    }

    public static List<BatteryQuantity> getBatteryQuantityByPrefix(int prefix) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:8080/api/calculations/quantityByBatteryCodePrefix";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
//                .queryParam("param2", material)
                .queryParam("param1", prefix);

        try {
            ResponseEntity<List<BatteryQuantity>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            // Log the error and return an empty list
            LoggerFactory.getLogger(LoadBatteryQuantity.class).error("Error fetching readiness data", e);
            return Collections.emptyList();
        }
    }
}
