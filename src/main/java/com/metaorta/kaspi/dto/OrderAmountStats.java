package com.metaorta.kaspi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderAmountStats {
    private Integer completedOrders;
    private Integer cancelledOrders;
    private Integer returnedOrders;
}
