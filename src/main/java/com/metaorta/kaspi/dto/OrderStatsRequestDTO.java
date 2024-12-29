package com.metaorta.kaspi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderStatsRequestDTO {
    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate endDate;

    @NotNull(message = "Merchant ID is required")
    private Integer merchantId;
}