package com.utetra.dto;

import com.utetra.entity.CartItem;

import java.math.BigDecimal;

public record CartLine(CartItem item, BigDecimal unitPrice, BigDecimal lineTotal) {

    // JSP EL nhận JavaBean getter, không nhận trực tiếp accessor của record.
    public CartItem getItem() {
        return item;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
