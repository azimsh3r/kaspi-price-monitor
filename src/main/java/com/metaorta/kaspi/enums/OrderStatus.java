package com.metaorta.kaspi.enums;

public enum OrderStatus {
    ACCEPTED_BY_MERCHANT,
    CANCELLED,
    UNKNOWN,
    RETURNED;

    public static OrderStatus fromStatus(String status) {
        return switch (status) {
            case "ACCEPTED_BY_MERCHANT" -> ACCEPTED_BY_MERCHANT;
            case "CANCELLED" -> CANCELLED;
            case "RETURNED" -> RETURNED;
            default -> UNKNOWN;
        };
    }
}