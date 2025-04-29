package com.metaorta.kaspi.service.order;

import com.metaorta.kaspi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class OrderStatisticsService {

    private final OrderRepository orderRepository;

    public BigDecimal getOrderAmountStats(ZonedDateTime startDate, ZonedDateTime endDate, Long merchantId) {
        return orderRepository.sumOrderTotalPriceBetweenDates(startDate, endDate, merchantId);
    }

    public BigDecimal getOrderRevenueStats(ZonedDateTime startDate, ZonedDateTime endDate, Long merchantId) {
        return orderRepository.sumOrderRevenueBetweenDates(startDate, endDate, merchantId);
    }
}
