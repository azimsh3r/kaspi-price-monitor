package com.metaorta.kaspi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderEntryDTO;
import com.metaorta.kaspi.enums.OrderStatus;
import com.metaorta.kaspi.dto.OrderAmountStats;
import com.metaorta.kaspi.dto.OrderRevenueStats;
import com.metaorta.kaspi.model.Order;
import com.metaorta.kaspi.model.OrderEntry;
import com.metaorta.kaspi.repository.OrderEntryRepository;
import com.metaorta.kaspi.repository.OrderRepository;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final ModelMapper modelMapper;

    private final OrderRepository orderRepository;

    private final OrderEntryRepository orderEntryRepository;

    @Value("${api.kaspi.base-url}")
    private String apiUrl;

    @Value("${api.kaspi.token}")
    private String token;

    public OrderService(CloseableHttpClient httpClient, ObjectMapper objectMapper, ModelMapper modelMapper, OrderRepository orderRepository, OrderEntryRepository orderEntryRepository) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
        this.orderEntryRepository = orderEntryRepository;
    }

    //TODO: run the scheduler every 24 hours
    public void syncOrdersScheduler() {
        //TODO: run a scheduler to sync orders every 24 hours
    }

    public void syncOrders(String startDate, String endDate) {
        List<OrderDTO> orderDTOS = getOrders(startDate, endDate);

        Set<String> existingOrderIds = new HashSet<>(
                orderRepository.findOrderIdsByOrderIdIn(
                        orderDTOS.stream().map(OrderDTO::getOrderId).collect(Collectors.toList())
                )
        );

        List<Order> ordersToSave = new ArrayList<>();
        List<OrderEntry> orderEntriesToSave = new ArrayList<>();

        orderDTOS.stream()
                .filter(orderDTO -> !existingOrderIds.contains(orderDTO.getOrderId())) // Skip duplicates
                .forEach(orderDTO -> {
                    Order order = new Order();
                    order.setPreOrder(orderDTO.getPreOrder());
                    order.setCreatedAt(orderDTO.getOrderDate());
                    order.setCustomerName(orderDTO.getCustomerName());
                    order.setCustomerPhoneNumber(orderDTO.getCustomerPhoneNumber());
                    order.setTotalPrice(orderDTO.getTotalPrice());
                    order.setOrderId(orderDTO.getOrderId());

                    ordersToSave.add(order);

                    orderDTO.getOrderEntryDTOS().forEach(orderEntryDTO -> {
                        OrderEntry orderEntry = modelMapper.map(orderEntryDTO, OrderEntry.class);
                        orderEntry.setOrder(order);
                        orderEntriesToSave.add(orderEntry);
                    });
                });

        orderRepository.saveAll(ordersToSave);
        orderEntryRepository.saveAll(orderEntriesToSave);
    }

    public List<OrderDTO> getOrders(String startDate, String endDate) {
        validateDates(startDate, endDate);
        long startMillis = parseDateToMillis(startDate);
        long endMillis = parseDateToMillis(endDate);

        List<OrderDTO> orders = new ArrayList<>();
        int currentPage = 0;
        int totalPages;

        do {
            JsonNode responseJson = fetchOrdersFromApi(startMillis, endMillis, OrderStatus.DEFAULT, currentPage);
            totalPages = responseJson.path("meta").path("pageCount").asInt(1);

            for (JsonNode orderNode : responseJson.path("data")) {
                OrderDTO order = parseOrder(orderNode);
                List<OrderEntryDTO> orderEntries = getOrderProducts(order.getOrderId());

                order.setOrderEntryDTOS(orderEntries);

                orders.add(order);
            }
            currentPage++;
        } while (currentPage < totalPages);

        return orders;
    }

    private List<OrderEntryDTO> getOrderProducts(String orderId) {
        String url = apiUrl + "/orders/" + orderId + "/entries";
        HttpGet request = createHttpRequest(url);

        List<OrderEntryDTO> products = new ArrayList<>();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            JsonNode responseJson = objectMapper.readTree(EntityUtils.toString(response.getEntity()));

            for (JsonNode productNode : responseJson.path("data")) {
                products.add(objectMapper.treeToValue(productNode, OrderEntryDTO.class));
            }
        } catch (Exception e) {
            logError("Failed to fetch order products", e);
        }
        return products;
    }

    public OrderAmountStats getOrderAmountStats(String startDate, String endDate) {
        validateDates(startDate, endDate);
        long startMillis = parseDateToMillis(startDate);
        long endMillis = parseDateToMillis(endDate);

        return new OrderAmountStats(
                getTotalCountByStatus(startMillis, endMillis, OrderStatus.ACCEPTED_BY_MERCHANT),
                getTotalCountByStatus(startMillis, endMillis, OrderStatus.CANCELLED),
                getTotalCountByStatus(startMillis, endMillis, OrderStatus.RETURNED)
        );
    }

    public OrderRevenueStats getOrderRevenueStats(String startDate, String endDate) {
        validateDates(startDate, endDate);
        long startMillis = parseDateToMillis(startDate);
        long endMillis = parseDateToMillis(endDate);

        return new OrderRevenueStats(
                getTotalRevenueByStatus(startMillis, endMillis, OrderStatus.ACCEPTED_BY_MERCHANT),
                getTotalRevenueByStatus(startMillis, endMillis, OrderStatus.CANCELLED),
                getTotalRevenueByStatus(startMillis, endMillis, OrderStatus.RETURNED)
        );
    }

    //TODO: seek the data from orders where status = :status and id = :merchantId
    private int getTotalCountByStatus(long startMillis, long endMillis, OrderStatus status) {
        JsonNode responseJson = fetchOrdersFromApi(startMillis, endMillis, status, 0);
        return responseJson.path("meta").path("totalCount").asInt(0);
    }

    //TODO: seek date from db instead of request
    private int getTotalRevenueByStatus(long startMillis, long endMillis, OrderStatus status) {
        int revenue = 0;
        int currentPage = 0;
        int totalPages;

        do {
            JsonNode responseJson = fetchOrdersFromApi(startMillis, endMillis, status, currentPage);
            totalPages = responseJson.path("meta").path("pageCount").asInt(1);

            for (JsonNode orderNode : responseJson.path("data")) {
                revenue += orderNode.path("attributes").path("totalPrice").asInt(0);
            }
            currentPage++;
        } while (currentPage < totalPages);
        return revenue;
    }

    private JsonNode fetchOrdersFromApi(long startMillis, long endMillis, OrderStatus status, int page) {
        String url = apiUrl + "/orders?page[number]=" + page + "&page[size]=100" +
                "&filter[orders][state]=ARCHIVE&filter[orders][creationDate][$ge]=" + startMillis +
                "&filter[orders][creationDate][$le]=" + endMillis;
        if (status != OrderStatus.DEFAULT) {
            url += "&filter[orders][status]=" + status;
        }

        System.out.println(url);

        HttpGet request = createHttpRequest(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            System.out.println("response received");

            String json = EntityUtils.toString(response.getEntity());
            System.out.println(json);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logError("Failed to fetch orders from API", e);
            throw new RuntimeException("API fetch failed", e);
        }
    }

    private HttpGet createHttpRequest(String url) {
        HttpGet request = new HttpGet(url);
        request.addHeader("X-Auth-Token", token);
        request.addHeader("Accept", "*/*");
        request.addHeader("Content-Type", "application/vnd.api+json");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
        return request;
    }

    private OrderDTO parseOrder(JsonNode orderNode) {
        try {
            return objectMapper.treeToValue(orderNode, OrderDTO.class);
        } catch (Exception e) {
            logError("Failed to parse order", e);
            throw new RuntimeException("Order parsing failed", e);
        }
    }

    private void validateDates(String startDate, String endDate) {
        if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
            throw new IllegalArgumentException("Start date and end date must not be empty");
        }
    }

    private long parseDateToMillis(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void logError(String message, Exception e) {
        // Replace with a logging framework like SLF4J
        System.err.println(message);
        e.printStackTrace();
    }
}
