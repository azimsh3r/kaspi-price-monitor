package com.metaorta.kaspi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class OrderProductDTO {
    private int quantity;

    private int totalPrice;

    private String code;

    private String name;

    @JsonProperty("attributes")
    public void unpackAttributes(Attributes attributes) {
        this.quantity = attributes.quantity;
        this.totalPrice = attributes.totalPrice;

        this.code = attributes.getCode();
        this.name = attributes.getName();
    }

    @Setter
    @Getter
    public static class Attributes {
        @JsonProperty("quantity")
        private int quantity;

        @JsonProperty("totalPrice")
        private int totalPrice;

        private String code;

        private String name;

        @JsonProperty("offer")
        public void unpackOffer(Offer offer) {
            this.code = offer.code;
            this.name = offer.name;
        }

        @JsonIgnoreProperties
        public static class Offer {
            @JsonProperty("code")
            private String code;

            @JsonProperty("name")
            private String name;
        }
    }
}
