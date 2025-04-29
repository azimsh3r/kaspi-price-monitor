package com.metaorta.kaspi.service.order.scheduler;

import com.metaorta.kaspi.model.Merchant;
import com.metaorta.kaspi.repository.MerchantRepository;
import com.metaorta.kaspi.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderSyncScheduler {

    private final OrderService orderService;
    private final MerchantRepository merchantRepository;

    @Scheduled(cron = "0 0 1 * * *") // Every day at 1 AM
    public void syncOrdersScheduler() {
        ZonedDateTime endDate = ZonedDateTime.now();
        ZonedDateTime startDate = endDate.minusDays(1);

        List<Merchant> merchants = merchantRepository.findAll();
        merchants.forEach(merchant ->
                orderService.syncOrders(startDate, endDate, merchant.getId())
        );
    }
}
