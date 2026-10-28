package com.utetra.dto;

import java.math.BigDecimal;

public record VnpayPaymentRequest(String transactionRef, String orderCode, BigDecimal amount) {
}
