package com.metaorta.kaspi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderProductDTO;
import com.metaorta.kaspi.model.OrderAmountStats;
import com.metaorta.kaspi.enums.OrderStatus;
import com.metaorta.kaspi.model.OrderRevenueStats;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO: add token retrieval logic to the database
@Service
public class OrderService {
    private final CloseableHttpClient httpClient;

    @Autowired
    public OrderService(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    //TODO: Find out the paging strategy
    public List<OrderDTO> getOrders(String startDate, String endDate, Integer merchantId) throws ParseException {
        //TODO: fetch token from postgresql
        String token = "uLFl1Mhpq1cDDiMltbmYZy9OJInG1UUYbb851osILCc=";

        List<OrderDTO> orders = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");

        Date sDate = sdf.parse(startDate);
        Date eDate = sdf.parse(endDate);

        int pageCount = 0;
        int counter = 0;

        do {
            String url = "https://kaspi.kz/shop/api/v2/orders" +
                    "?page[number]=" + (pageCount - 1) +
                    "&page[size]=100" +
                    "&filter[orders][state]=ARCHIVE" +
                    "&filter[orders][creationDate][$ge]=" + sDate.getTime() +
                    "&filter[orders][creationDate][$le]=" + eDate.getTime();

            HttpGet request = new HttpGet(url);
            request.addHeader("X-Auth-Token", token);
            request.addHeader("Accept", "*/*");
            request.addHeader("Content-Type", "application/vnd.api+json");
            request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");


            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode rootNode = objectMapper.readTree(json);
                JsonNode dataNode = rootNode.get("data");

                if (pageCount == 0) {
                    JsonNode metaNode = rootNode.get("meta");

                    if (metaNode != null) {
                        pageCount = metaNode.get("pageCount").asInt();
                    }
                }

                if (dataNode.isArray()) {
                    for (JsonNode node : dataNode) {
                        OrderDTO order = objectMapper.treeToValue(node, OrderDTO.class);

                        List<OrderProductDTO> products = getOrderProducts(order.getOrderId(), token);

                        for (OrderProductDTO product : products) {
                            order.setCode(product.getCode());
                            order.setName(product.getName());
                            order.setTotalPrice(product.getTotalPrice());
                            order.setQuantity(product.getQuantity());

                            orders.add(order);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter++;
        } while (counter < pageCount);

        return orders;
    }

    private List<OrderProductDTO> getOrderProducts(String orderId, String token) {
        String url = "https://kaspi.kz/shop/api/v2/orders/" + orderId + "/entries";

        HttpGet request = new HttpGet(url);
        request.addHeader("X-Auth-Token", token);
        request.addHeader("Accept", "*/*");
        request.addHeader("Content-Type", "application/vnd.api+json");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        List<OrderProductDTO> products = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");

            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    OrderProductDTO order = objectMapper.treeToValue(node, OrderProductDTO.class);
                    products.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public OrderAmountStats getOrderAmountStats(String startDate, String endDate, Integer merchantId) throws ParseException, JsonProcessingException {
        String token = "uLFl1Mhpq1cDDiMltbmYZy9OJInG1UUYbb851osILCc=";

        String completedJson = getOrderResponseByStatus(startDate, endDate, token, OrderStatus.ACCEPTED_BY_MERCHANT);
        String cancelledJson = getOrderResponseByStatus(startDate, endDate, token, OrderStatus.CANCELLED);
        String returnedJson = getOrderResponseByStatus(startDate, endDate, token, OrderStatus.RETURNED);

        OrderAmountStats orderAmountStats = new OrderAmountStats();

        orderAmountStats.setCompletedOrders(
                getOrderAmount(completedJson)
        );
        orderAmountStats.setCancelledOrders(
                getOrderAmount(cancelledJson)
        );
        orderAmountStats.setReturnedOrders(
                getOrderAmount(returnedJson)
        );
        return orderAmountStats;
    }

    public OrderRevenueStats getOrderRevenueStats(String startDate, String endDate, Integer merchantId) {
        return null;
    }

    private Integer getOrderAmount(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode metaNode = rootNode.get("meta");

        int totalCount = 0;
        if (metaNode != null) {
            totalCount = metaNode.get("totalCount").asInt();
        }
        return totalCount;
    }

    private String getOrderResponseByStatus(String startDate, String endDate, String token, OrderStatus orderStatus) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");

        String status = "COMPLETED";
        switch (orderStatus) {
            case ACCEPTED_BY_MERCHANT: {
                status = "COMPLETED";
                break;
            }
            case CANCELLED: {
                status = "CANCELLED";
                break;
            }
            case RETURNED: {
                status = "RETURNED";
                break;
            }
        }

        Date sDate = sdf.parse(startDate);
        Date eDate = sdf.parse(endDate);

        String url = "https://kaspi.kz/shop/api/v2/orders" +
                "?page[number]=0&page[size]=100" +
                "&filter[orders][state]=ARCHIVE" +
                "&filter[orders][creationDate][$ge]=" + sDate.getTime() +
                "&filter[orders][creationDate][$le]=" + eDate.getTime() +
                "&filter[orders][status]=" + status;

        HttpGet request = new HttpGet(url);
        request.addHeader("X-Auth-Token", token);
        request.addHeader("Accept", "*/*");
        request.addHeader("Content-Type", "application/vnd.api+json");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                System.err.println("Error: API responded with status code " + statusCode);
            } else {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
