package com.metaorta.kaspi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderProductDTO;
import com.metaorta.kaspi.dto.OrderStatsDTO;
import com.metaorta.kaspi.enums.OrderStatus;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OrderService {
    private final CloseableHttpClient httpClient;

    @Autowired
    public OrderService(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<OrderDTO> getOrderList(String startDate, String endDate, Integer merchantId) throws ParseException {
        //TODO: fetch token from postgresql
        String token = "17LXGV0OswBHvVIsxheI+PXOfIzGjJ2n8+rncYOE62I=";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");

        Date sDate = sdf.parse(startDate);
        Date eDate = sdf.parse(endDate);

        String url = "https://kaspi.kz/shop/api/v2/orders" +
                "?page[number]=0&page[size]=100" +
                "&filter[orders][state]=NEW" +
                "&filter[orders][creationDate][$ge]=" + sDate.getTime() +
                "&filter[orders][creationDate][$le]=" + eDate.getTime();

        HttpGet request = new HttpGet(url);
        request.addHeader("X-Auth-Token", token);
        request.addHeader("Accept", "*/*");
        request.addHeader("Content-Type", "application/vnd.api+json");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        List<OrderDTO> orders = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            System.out.println(json);

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");

            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    OrderDTO order = objectMapper.treeToValue(node, OrderDTO.class);

                    List<OrderProductDTO> products = getOrderProducts(order.getOrderId(), token);

                    for (OrderProductDTO product : products) {
                        //TODO: add product name or sku to order details
                        order.setTotalPrice(product.getTotalPrice());
                        order.setQuantity(product.getQuantity());

                        orders.add(order);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public OrderStatsDTO getOrderStats(String startDate, String endDate, Integer merchantId) throws ParseException {
        //todo: add token retrieval from postgresql
        String token = "17LXGV0OswBHvVIsxheI+PXOfIzGjJ2n8+rncYOE62I=";

        List<OrderDTO> completedOrders = getOrderListByStatus(startDate, endDate, token, OrderStatus.ACCEPTED_BY_MERCHANT);
        List<OrderDTO> cancelledOrders = getOrderListByStatus(startDate, endDate, token, OrderStatus.CANCELLED);
        List<OrderDTO> returnedOrders = getOrderListByStatus(startDate, endDate, token, OrderStatus.RETURNED);

        AtomicReference<Long> completedRevenue = new AtomicReference<>(0L);
        AtomicReference<Long> cancelledRevenue = new AtomicReference<>(0L);
        AtomicReference<Long> returnedRevenue = new AtomicReference<>(0L);

        completedOrders.forEach(order -> completedRevenue.updateAndGet(v -> v + order.getTotalPrice()));
        cancelledOrders.forEach(order -> cancelledRevenue.updateAndGet(v -> v + order.getTotalPrice()));
        returnedOrders.forEach(order -> returnedRevenue.updateAndGet(v -> v + order.getTotalPrice()));

        OrderStatsDTO orderStatsDTO = new OrderStatsDTO();

        orderStatsDTO.setCancelledOrders(cancelledOrders.size());
        orderStatsDTO.setCompletedOrders(completedOrders.size());
        orderStatsDTO.setReturnedOrders(returnedOrders.size());

        orderStatsDTO.setCompletedRevenue(completedRevenue.get());
        orderStatsDTO.setCancelledRevenue(cancelledRevenue.get());
        orderStatsDTO.setReturnedRevenue(returnedRevenue.get());

        return orderStatsDTO;
    }

    private List<OrderDTO> getOrderListByStatus(String startDate, String endDate, String token, OrderStatus orderStatus) throws ParseException {
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

        List<OrderDTO> orderDTOS = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            System.out.println(json);

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");

            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    OrderDTO order = objectMapper.treeToValue(node, OrderDTO.class);
                    orderDTOS.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderDTOS;
    }
}
