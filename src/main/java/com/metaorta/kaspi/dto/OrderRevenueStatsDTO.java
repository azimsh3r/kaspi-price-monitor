package com.metaorta.kaspi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class OrderRevenueStatsDTO {
    private Integer completedRevenue;
    private Integer cancelledRevenue;
    private Integer returnedRevenue;
}
