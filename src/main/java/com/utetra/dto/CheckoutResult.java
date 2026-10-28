package com.utetra.dto;

import com.utetra.entity.Order;

public record CheckoutResult(Order order, VnpayPaymentRequest vnpayRequest) {

    public boolean requiresVnpay() {
        return vnpayRequest != null;
    }
}
