package com.monbat.services.api;

import com.monbat.models.dto.sap.PlannedOrder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class PlannedOrderApiClient {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static List<PlannedOrder> getData(LocalDate reqDelDateBegin, LocalDate reqDelDateEnd) {
        String apiUrl = MAIN_ADDRESS + "api/sap/getPlannedOrders";
        Base64 base64 = new Base64();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("username", new String(base64.encode("niliev".getBytes())))
                    .queryParam("password", new String(base64.encode("21Zaq12wsx!!".getBytes())))
                    .queryParam("reqDelDateBegin", reqDelDateBegin.atStartOfDay())
                    .queryParam("reqDelDateEnd", reqDelDateEnd.atTime(23, 59));

            ResponseEntity<List<PlannedOrder>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            LoggerFactory.getLogger(PlannedOrderApiClient.class).error("Error fetching production orders", e);
            return Collections.emptyList();
        }
    }
}

