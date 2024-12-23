package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.dto.OrderStatsDTO;
import com.metaorta.kaspi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDTO> getAll() {
        return orderService.getOrderList();
    }

    @GetMapping("/stats")
    public OrderStatsDTO getOrderStats() {
        return orderService.getOrderStats();
    }
}
