package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.OrderAmountStatsDTO;
import com.metaorta.kaspi.dto.OrderRevenueStatsDTO;
import com.metaorta.kaspi.model.Order;
import com.metaorta.kaspi.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/amount")
    public BigDecimal getOrderAmountStats(@RequestParam String startDate,
                                          @RequestParam String endDate,
                                          @RequestParam Long merchantId) {
        ZonedDateTime start = ZonedDateTime.parse(startDate);
        ZonedDateTime end = ZonedDateTime.parse(endDate);
        return orderService.getOrderAmountStats(start, end, merchantId);
    }

    @GetMapping("/revenue")
    public BigDecimal getOrderRevenueStats(@RequestParam String startDate,
                                           @RequestParam String endDate,
                                           @RequestParam Long merchantId) {
        ZonedDateTime start = ZonedDateTime.parse(startDate);
        ZonedDateTime end = ZonedDateTime.parse(endDate);
        return orderService.getOrderRevenueStats(start, end, merchantId);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncOrders(@RequestParam String startDate,
                                             @RequestParam String endDate,
                                             @RequestParam Integer merchantId) {
        ZonedDateTime start = ZonedDateTime.parse(startDate);
        ZonedDateTime end = ZonedDateTime.parse(endDate);
        orderService.syncOrders(start, end, merchantId);
        return new ResponseEntity<>("Orders are synchronized successfully", HttpStatus.OK);
    }
}
