package com.metaorta.kaspi.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderSyncService orderSyncService;
    private final OrderStatisticsService statisticsService;

    public void syncOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer merchantId) {
        orderSyncService.syncOrders(startDate, endDate, merchantId);
    }

    public BigDecimal getOrderAmountStats(ZonedDateTime startDate, ZonedDateTime endDate, Long merchantId) {
        return statisticsService.getOrderAmountStats(startDate, endDate, merchantId);
    }

    public BigDecimal getOrderRevenueStats(ZonedDateTime startDate, ZonedDateTime endDate, Long merchantId) {
        return statisticsService.getOrderRevenueStats(startDate, endDate, merchantId);
    }
}
