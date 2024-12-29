package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.*;
import com.metaorta.kaspi.model.Order;
import com.metaorta.kaspi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<Order> getAllByDate(@Valid @ModelAttribute OrderRequestDTO request) {
        return orderService.getOrdersFromDB(request.getStartDate(), request.getEndDate(), request.getMerchantId(), request.getPage(), request.getSize());
    }

    @GetMapping("/amount")
    public OrderAmountStatsDTO getOrderAmountStats(@Valid @ModelAttribute OrderStatsRequestDTO request) {
        return orderService.getOrderAmountStats(request.getStartDate(), request.getEndDate(), request.getMerchantId());
    }

    @GetMapping("/revenue")
    public OrderRevenueStatsDTO getOrderRevenueStats(@Valid @ModelAttribute OrderStatsRequestDTO request) {
        return orderService.getOrderRevenueStats(request.getStartDate(), request.getEndDate(), request.getMerchantId());
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncOrders(@Valid @RequestBody SyncOrdersRequestDTO request) {
        orderService.syncOrders(request.getStartDate(), request.getEndDate(), request.getMerchantId());
        return new ResponseEntity<>("Orders are synchronized successfully", HttpStatus.OK);
    }
}
