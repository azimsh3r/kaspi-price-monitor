package com.metaorta.kaspi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Setter
@Getter
public class ProductDTO {
    private String sku;
    private String model;
    private Integer price;
}
