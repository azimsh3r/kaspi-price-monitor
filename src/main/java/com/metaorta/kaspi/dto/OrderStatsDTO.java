package com.metaorta.kaspi.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderStatsDTO {
    private Integer completedOrders;
    private Integer cancelledOrders;
    private Integer returnedOrders;

    private Long completedRevenue;
    private Long cancelledRevenue;
    private Long returnedRevenue;
}
