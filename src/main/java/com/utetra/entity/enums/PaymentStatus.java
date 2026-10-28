package com.utetra.entity.enums;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán", "text-bg-warning"),
    PAID("Đã thanh toán", "text-bg-success"),
    FAILED("Thanh toán thất bại", "text-bg-danger");

    private final String label;
    private final String badgeClass;

    PaymentStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
