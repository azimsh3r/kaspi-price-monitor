package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.OrderAmountStats;
import com.metaorta.kaspi.dto.OrderRevenueStats;
import com.metaorta.kaspi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//TODO: think about api design
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/amount")
    public OrderAmountStats getOrderAmountStats(@RequestParam(required = false) Integer id) {
        //TODO: validate amount to be not empty
        return orderService.getOrderAmountStats("2024-12-12", "2024-12-23");
    }

    @GetMapping("/revenue")
    public OrderRevenueStats getOrderRevenueStats() {
        return orderService.getOrderRevenueStats("2024-12-12", "2024-12-23");
    }

    @GetMapping("/sync")
    public String syncOrders() {
        orderService.syncOrders("2024-12-12", "2024-12-23");
        return "Orders are synchronized!";
    }
}
