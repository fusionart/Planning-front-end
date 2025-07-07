package com.monbat.services.api;

import com.monbat.models.entities.Material;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class MaterialApiClient {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static List<Material> getData() {
        String apiUrl = MAIN_ADDRESS + "api/sap/getMaterials";
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl);

            ResponseEntity<List<Material>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            LoggerFactory.getLogger(MaterialApiClient.class).error("Error fetching production orders", e);
            return Collections.emptyList();
        }
    }
}
