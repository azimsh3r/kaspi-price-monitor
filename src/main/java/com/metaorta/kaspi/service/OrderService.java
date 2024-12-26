package com.metaorta.kaspi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderEntryDTO;
import com.metaorta.kaspi.enums.OrderStatus;
import com.metaorta.kaspi.dto.OrderAmountStatsDTO;
import com.metaorta.kaspi.dto.OrderRevenueStatsDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final ModelMapper modelMapper;

    private final OrderRepository orderRepository;

    private final OrderEntryRepository orderEntryRepository;

    private final RestTemplate restTemplate;

    @Value("${api.kaspi.base-url}")
    private String apiUrl;

    @Value("${api.kaspi.token}")
    private String token;

    public OrderService(CloseableHttpClient httpClient, ObjectMapper objectMapper, ModelMapper modelMapper, OrderRepository orderRepository, OrderEntryRepository orderEntryRepository, RestTemplate restTemplate) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
        this.orderEntryRepository = orderEntryRepository;
        this.restTemplate = restTemplate;
    }

    public void syncOrdersScheduler(Integer merchantId) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                //TODO: add stop mechanism
                LocalDate lastOrder = orderRepository.findLastOrderCreatedAt().toLocalDate();

                String now = LocalDate.now().toString();

                syncOrders(String.valueOf(lastOrder), now, merchantId);
                System.out.println("SyncOrders task completed successfully");
            } catch (Exception e) {
                System.err.println("Error executing syncOrders: " + e.getMessage());
            }
        };

        scheduledExecutorService.scheduleWithFixedDelay(task, 0, 24, TimeUnit.HOURS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(5, TimeUnit.MINUTES)) {
                    scheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }

    public void syncOrders(String startDate, String endDate, Integer id) {
        List<OrderDTO> orderDTOS = fetchOrders(startDate, endDate, id);

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
                    Order order = convertDTOToOrder(orderDTO);

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

    private static Order convertDTOToOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setPreOrder(orderDTO.getPreOrder());
        order.setCreatedAt(orderDTO.getOrderDate());
        order.setCustomerName(orderDTO.getCustomerName());
        order.setCustomerPhoneNumber(orderDTO.getCustomerPhoneNumber());
        order.setTotalPrice(orderDTO.getTotalPrice());
        order.setOrderId(orderDTO.getOrderId());
        order.setOrderStatus(orderDTO.getOrderStatus());
        return order;
    }

    public OrderAmountStatsDTO getOrderAmountStats(LocalDate startDate, LocalDate endDate, Integer id) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return new OrderAmountStatsDTO(
                orderRepository.countAllByOrderStatus(OrderStatus.ACCEPTED_BY_MERCHANT, startDateTime, endDateTime),
                orderRepository.countAllByOrderStatus(OrderStatus.CANCELLED, startDateTime, endDateTime),
                orderRepository.countAllByOrderStatus(OrderStatus.RETURNED, startDateTime, endDateTime)
        );
    }

    public OrderRevenueStatsDTO getOrderRevenueStats(LocalDate startDate, LocalDate endDate, Integer id) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return new OrderRevenueStatsDTO(
                orderRepository.findRevenueByOrderStatus(OrderStatus.ACCEPTED_BY_MERCHANT, startDateTime, endDateTime),
                orderRepository.findRevenueByOrderStatus(OrderStatus.CANCELLED, startDateTime, endDateTime),
                orderRepository.findRevenueByOrderStatus(OrderStatus.RETURNED, startDateTime, endDateTime)
        );
    }

    public Page<Order> getOrdersFromDB(String startDate, String endDate, Integer merchantId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        return orderRepository.findByCreatedAtBetween(startDateTime, endDateTime, pageable);
    }

    public List<OrderDTO> fetchOrders(String startDate, String endDate, Integer id) {
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
                List<OrderEntryDTO> orderEntries = fetchOrderProducts(order.getOrderId());

                order.setOrderEntryDTOS(orderEntries);

                orders.add(order);
            }
            currentPage++;
        } while (currentPage < totalPages);

        return orders;
    }

    private List<OrderEntryDTO> fetchOrderProducts(String orderId) {
        String url = apiUrl + "/orders/" + orderId + "/entries";

        List<OrderEntryDTO> products = new ArrayList<>();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    JsonNode.class
            );

            for (JsonNode productNode : response.getBody().path("data")) {
                products.add(objectMapper.treeToValue(productNode, OrderEntryDTO.class));
            }
        } catch (RestClientException e) {
            logError("Failed to fetch order products", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    private OrderDTO parseOrder(JsonNode orderNode) {
        try {
            return objectMapper.treeToValue(orderNode, OrderDTO.class);
        } catch (Exception e) {
            logError("Failed to parse order", e);
            throw new RuntimeException("Order parsing failed", e);
        }
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

    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        headers.set("Accept", "*/*");
        headers.set("Content-Type", "application/vnd.api+json");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        return new HttpEntity<>(headers);
    }

    private void validateDates(String startDate, String endDate) {
        if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
            throw new IllegalArgumentException("Start date and end date must not be empty");
        }
    }

    private long parseDateToMillis(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);

        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void logError(String message, Exception e) {
        // Replace with a logging framework like SLF4J
        System.err.println(message);
        e.printStackTrace();
    }
}
