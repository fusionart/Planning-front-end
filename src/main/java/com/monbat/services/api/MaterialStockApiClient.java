package com.monbat.services.api;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class MaterialStockApiClient {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static Double getData(String material) {
        String apiUrl = MAIN_ADDRESS + "api/sap/getMaterialStock";
        Base64 base64 = new Base64();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("username", new String(base64.encode("niliev".getBytes())))
                    .queryParam("password", new String(base64.encode("21Zaq12wsx!!".getBytes())))
                    .queryParam("material", material);

            ResponseEntity<Double> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : (double) 0;
        } catch (Exception e) {
            LoggerFactory.getLogger(MaterialStockApiClient.class).error("Error fetching production orders", e);
            return (double) 0;
        }
    }
}

