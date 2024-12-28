package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.dto.OrderAmountStatsDTO;
import com.metaorta.kaspi.dto.OrderRevenueStatsDTO;
import com.metaorta.kaspi.model.Order;
import com.metaorta.kaspi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<Order> getAllByDate(@RequestParam String startDate, @RequestParam String endDate, @RequestParam Integer merchantId, @RequestParam Integer page, @RequestParam Integer size) {
        return orderService.getOrdersFromDB(startDate, endDate, merchantId, page, size);
    }

    @GetMapping("/amount")
    public OrderAmountStatsDTO getOrderAmountStats(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
                                                   @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
                                                   @RequestParam Integer merchantId) {
        return orderService.getOrderAmountStats(startDate, endDate, merchantId);
    }


    @GetMapping("/revenue")
    public OrderRevenueStatsDTO getOrderRevenueStats(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
                                                     @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
                                                     @RequestParam Integer merchantId) {
        return orderService.getOrderRevenueStats(startDate, endDate, merchantId);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncOrders(@RequestParam String startDate, @RequestParam String endDate, @RequestParam Integer merchantId) {
        orderService.syncOrders(startDate, endDate, merchantId);
        return new ResponseEntity<>("Orders are synchronized successfully", HttpStatus.OK);
    }
}
