package com.utetra.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartView(List<CartLine> lines, int totalQuantity, BigDecimal subtotal) {

    // JSP EL nhận JavaBean getter, không nhận trực tiếp accessor của record.
    public List<CartLine> getLines() {
        return lines;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public static CartView empty() {
        return new CartView(List.of(), 0, BigDecimal.ZERO);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
