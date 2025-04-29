package com.metaorta.kaspi.service.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderEntryDTO;
import com.metaorta.kaspi.enums.OrderStatus;
import com.metaorta.kaspi.util.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderApiClient {

    private CloseableHttpClient httpClient;

    @Value("${api.kaspi.base-url}")
    private String apiUrl;

    @Value("${api.kaspi.token}")
    private String token;

    private final ObjectMapper objectMapper;

    private final OrderMapper orderMapper;

    public List<OrderDTO> fetchOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer merchantId) {
        long startMillis = parseDateToMillis(startDate);
        long endMillis = parseDateToMillis(endDate);

        List<OrderDTO> orders = new ArrayList<>();
        int currentPage = 0;
        int totalPages;

        do {
            JsonNode responseJson = fetchOrdersFromApi(startMillis, endMillis, OrderStatus.DEFAULT, currentPage);
            totalPages = responseJson.path("meta").path("pageCount").asInt(1);

            for (JsonNode orderNode : responseJson.path("data")) {
                OrderDTO order = orderMapper.parseOrder(orderNode);
                List<OrderEntryDTO> orderEntries = orderMapper.fetchOrderProductsFromJson(apiUrl, token, order.getOrderId());

                order.setOrderEntryDTOS(orderEntries);

                orders.add(order);
            }
            currentPage++;
        } while (currentPage < totalPages);

        return orders;
    }

    private JsonNode fetchOrdersFromApi(long startMillis, long endMillis, OrderStatus status, int page) {
        String url = apiUrl + "/orders?page[number]=" + page + "&page[size]=100" +
                "&filter[orders][state]=ARCHIVE&filter[orders][creationDate][$ge]=" + startMillis +
                "&filter[orders][creationDate][$le]=" + endMillis;
        if (status != OrderStatus.DEFAULT) {
            url += "&filter[orders][status]=" + status;
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("X-Auth-Token", token);
        request.addHeader("Accept", "*/*");
        request.addHeader("Content-Type", "application/vnd.api+json");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("API fetch failed", e);
        }
    }

    private long parseDateToMillis(ZonedDateTime dateTime) {
        return dateTime.toInstant().toEpochMilli();
    }
}
