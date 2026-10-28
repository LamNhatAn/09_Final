package com.utetra.dto;

import com.utetra.entity.enums.PaymentStatus;
import lombok.Getter;

@Getter
public class VnpayCallbackOutcome {

    public enum Type {
        PROCESSED, ALREADY_PROCESSED, INVALID_SIGNATURE, NOT_FOUND, INVALID_AMOUNT, INVALID_REQUEST
    }

    private final Type type;
    private final String orderCode;
    private final PaymentStatus paymentStatus;

    public VnpayCallbackOutcome(Type type, String orderCode, PaymentStatus paymentStatus) {
        this.type = type;
        this.orderCode = orderCode;
        this.paymentStatus = paymentStatus;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
}
