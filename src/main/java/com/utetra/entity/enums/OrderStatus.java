package com.utetra.entity.enums;

public enum OrderStatus {
    PENDING("Chờ xác nhận", "text-bg-warning"),
    CONFIRMED("Đã xác nhận", "text-bg-info"),
    PREPARING("Đang pha chế", "text-bg-primary"),
    DELIVERING("Đang giao", "text-bg-secondary"),
    COMPLETED("Hoàn thành", "text-bg-success"),
    CANCELLED("Đã hủy", "text-bg-danger");

    private final String label;
    private final String badgeClass;

    OrderStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public OrderStatus next() {
        return switch (this) {
            case PENDING -> CONFIRMED;
            case CONFIRMED -> PREPARING;
            case PREPARING -> DELIVERING;
            case DELIVERING -> COMPLETED;
            case COMPLETED, CANCELLED -> null;
        };
    }
}
