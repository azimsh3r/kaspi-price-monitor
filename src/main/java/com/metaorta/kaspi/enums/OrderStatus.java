package com.metaorta.kaspi.enums;

public enum OrderStatus {
    ACCEPTED_BY_MERCHANT,
    CANCELLED,
    DEFAULT,
    RETURNED;

    public static OrderStatus fromStatus(String status) {
        return switch (status) {
            case "COMPLETED" -> ACCEPTED_BY_MERCHANT;
            case "CANCELLED" -> CANCELLED;
            case "RETURNED" -> RETURNED;
            default -> DEFAULT;
        };
    }

    public static String toString(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case ACCEPTED_BY_MERCHANT -> "ACCEPTED_BY_MERCHANT";
            case CANCELLED -> "CANCELLED";
            case DEFAULT -> null;
            case RETURNED -> "RETURNED";
        };
    }
}