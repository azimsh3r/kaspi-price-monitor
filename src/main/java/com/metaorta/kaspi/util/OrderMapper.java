package com.metaorta.kaspi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderEntryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderMapper(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public OrderDTO parseOrder(JsonNode orderNode) {
        try {
            return objectMapper.treeToValue(orderNode, OrderDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Order parsing failed", e);
        }
    }

    public List<OrderEntryDTO> fetchOrderProductsFromJson(String apiUrl, String token, String orderId) {
        String url = apiUrl + "/orders/" + orderId + "/entries";

        List<OrderEntryDTO> products = new ArrayList<>();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(token),
                    JsonNode.class
            );

            for (JsonNode productNode : response.getBody().path("data")) {
                products.add(objectMapper.treeToValue(productNode, OrderEntryDTO.class));
            }
        } catch (RestClientException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    private HttpEntity<String> createHttpEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/vnd.api+json");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        return new HttpEntity<>(headers);
    }
}
