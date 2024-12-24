package com.metaorta.kaspi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class OrderRevenueStats {
    private Long completedRevenue;
    private Long cancelledRevenue;
    private Long returnedRevenue;
}
