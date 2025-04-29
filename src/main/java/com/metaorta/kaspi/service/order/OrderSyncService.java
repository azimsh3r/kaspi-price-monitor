package com.metaorta.kaspi.service.order;

import com.metaorta.kaspi.dto.OrderDTO;
import com.metaorta.kaspi.model.Order;
import com.metaorta.kaspi.model.OrderEntry;
import com.metaorta.kaspi.repository.OrderEntryRepository;
import com.metaorta.kaspi.repository.OrderRepository;
import com.metaorta.kaspi.service.order.client.OrderApiClient;
import com.metaorta.kaspi.util.OrderMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderSyncService {

    private final OrderApiClient apiClient;
    private final OrderRepository orderRepository;
    private final OrderEntryRepository orderEntryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void syncOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer merchantId) {
        List<OrderDTO> orderDTOS = apiClient.fetchOrders(startDate, endDate, merchantId);

        Set<String> existingOrderIds = new HashSet<>(
                orderRepository.findOrderIdsByOrderIdInAndMerchantId(
                        orderDTOS.stream().map(OrderDTO::getOrderId).collect(Collectors.toList()),
                        merchantId
                )
        );

        List<Order> ordersToSave = new ArrayList<>();
        List<OrderEntry> orderEntriesToSave = new ArrayList<>();

        orderDTOS.stream()
                .filter(orderDTO -> !existingOrderIds.contains(orderDTO.getOrderId())) // Skip duplicates
                .forEach(orderDTO -> {
                    Order order = modelMapper.map(orderDTO, Order.class);

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
}
