package com.metaorta.kaspi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderRequestDTO {
    @NotNull(message = "Start date is required")
    @Pattern(regexp = "\\d{2}-\\d{2}-\\d{4}", message = "Start date must be in the format dd-MM-yyyy")
    private String startDate;

    @NotNull(message = "End date is required")
    @Pattern(regexp = "\\d{2}-\\d{2}-\\d{4}", message = "End date must be in the format dd-MM-yyyy")
    private String endDate;

    @NotNull(message = "Merchant ID is required")
    @Min(value = 1, message = "Merchant ID must be greater than 0")
    private Integer merchantId;

    @NotNull(message = "Page is required")
    @Min(value = 0, message = "Page must be 0 or greater")
    private Integer page;

    @NotNull(message = "Size is required")
    @Min(value = 1, message = "Size must be 1 or greater")
    private Integer size;
}

