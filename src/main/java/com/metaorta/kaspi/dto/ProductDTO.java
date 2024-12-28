package com.metaorta.kaspi.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductDTO {
    private String sku;
    private String model;
    private Integer price;

    @Override
    public String toString() {
        return "ProductDTO{" +
                "sku='" + sku + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                '}';
    }

    public int getPrice() {
        return price;
    }

    public ProductDTO(String sku, String model, Integer price) {
        this.sku = sku;
        this.model = model;
        this.price = price;
    }





}
