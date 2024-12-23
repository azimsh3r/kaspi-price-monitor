package com.metaorta.kaspi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

//TODO: add product details
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class OrderProductDTO {
    private int quantity;
    private int totalPrice;

    @JsonProperty("attributes")
    public void unpackAttributes(Attributes attributes) {
        this.quantity = attributes.quantity;
        this.totalPrice = attributes.totalPrice;
    }

    public static class Attributes {
        @JsonProperty("quantity")
        private int quantity;

        @JsonProperty("totalPrice")
        private int totalPrice;
    }
}
