package com.metaorta.kaspi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.model.OrderAmountStats;
import com.metaorta.kaspi.model.OrderRevenueStats;
import com.metaorta.kaspi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

//TODO: Accept startDate and endDate and merchantId
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDTO> getAll() throws ParseException {
        return orderService.getOrders("28-10-2024", "08-11-2024", 3);
    }

    @GetMapping("/amountStats")
    public OrderAmountStats getOrderAmountStats() throws ParseException, JsonProcessingException {
        return orderService.getOrderAmountStats("12-12-2024", "23-12-2024", 4);
    }

    @GetMapping("/revenueStats")
    public OrderRevenueStats getOrderRevenueStats() {
        return orderService.getOrderRevenueStats("12-12-2024", "23-12-2024", 4);
    }
}
