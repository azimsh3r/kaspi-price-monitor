package com.metaorta.kaspi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metaorta.kaspi.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class OrderDTO {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("attributes")
    private void unpackAttributes(Attributes attributes) {
        this.orderDate = Instant.ofEpochMilli(attributes.creationDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        this.customerName = attributes.customer.firstName + " " + attributes.customer.lastName;
        this.totalPrice = attributes.totalPrice;
        this.orderStatus = OrderStatus.fromStatus(attributes.status);
        this.preOrder = attributes.preOrder;
    }

    private LocalDateTime orderDate;
    private String customerName;
    private Integer totalPrice;
    private Integer quantity;
    private OrderStatus orderStatus;
    private Boolean preOrder;

    @JsonIgnore
    private String code;

    @JsonIgnore
    private String name;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Setter
    @Getter
    public static class Attributes {
        @JsonProperty("creationDate")
        public long creationDate;

        @JsonProperty("preorder")
        public Boolean preOrder;

        @JsonProperty("customer")
        public Customer customer;

        @JsonProperty("totalPrice")
        public Integer totalPrice;

        @JsonProperty("status")
        public String status;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Customer {
            @JsonProperty("firstName")
            public String firstName;

            @JsonProperty("lastName")
            public String lastName;
        }
    }
}
