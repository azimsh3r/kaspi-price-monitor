package com.metaorta.kaspi.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductDTO {
    private String sku;
    private String model;
    private double price;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

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

    public void setPrice(double price) {
        this.price = price;
    }

    public ProductDTO(String sku, String model, double price) {
        this.sku = sku;
        this.model = model;
        this.price = price;
    }





}
