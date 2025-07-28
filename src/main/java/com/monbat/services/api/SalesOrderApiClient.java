package com.monbat.services.api;

import com.monbat.models.dto.sap.sales_order.SalesOrder;
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

import static com.monbat.utils.Constants.*;

public class SalesOrderApiClient {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static List<SalesOrder> getData(LocalDate reqDelDateBegin, LocalDate reqDelDateEnd) {
        String apiUrl = MAIN_ADDRESS + "api/sap/getSalesOrders";
        Base64 base64 = new Base64();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("username", new String(base64.encode(SAP_USERNAME.getBytes())))
                    .queryParam("password", new String(base64.encode(SAP_PASSWORD.getBytes())))
                    .queryParam("reqDelDateBegin", reqDelDateBegin.atStartOfDay())
                    .queryParam("reqDelDateEnd", reqDelDateEnd.atTime(23, 59));

            ResponseEntity<List<SalesOrder>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            LoggerFactory.getLogger(SalesOrderApiClient.class).error("Error fetching production orders", e);
            return Collections.emptyList();
        }
    }
}
