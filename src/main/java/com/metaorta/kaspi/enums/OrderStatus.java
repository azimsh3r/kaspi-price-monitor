package com.metaorta.kaspi.enums;

import org.hibernate.query.Order;

public enum OrderStatus {
    ACCEPTED_BY_MERCHANT,
    CANCELLED,
    UNKNOWN,
    RETURNED;

    public static OrderStatus fromStatus(String status) {
        return switch (status) {
            case "COMPLETED" -> ACCEPTED_BY_MERCHANT;
            case "CANCELLED" -> CANCELLED;
            case "RETURNED" -> RETURNED;
            default -> UNKNOWN;
        };
    }

    public static String toString(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case ACCEPTED_BY_MERCHANT -> "ACCEPTED_BY_MERCHANT";
            case CANCELLED -> "CANCELLED";
            case UNKNOWN -> null;
            case RETURNED -> "RETURNED";
        };
    }
}