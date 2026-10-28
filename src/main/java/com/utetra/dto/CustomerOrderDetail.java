package com.utetra.dto;

import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;

import java.util.List;

public record CustomerOrderDetail(Order order, List<OrderStatusHistory> history) {

    public Order getOrder() {
        return order;
    }

    public List<OrderStatusHistory> getHistory() {
        return history;
    }

    public boolean isCancellable() {
        return order.getStatus() == OrderStatus.PENDING
                && !(order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.FAILED);
    }

    public boolean isPayable() {
        return order.getStatus() == OrderStatus.PENDING
                && order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.PAID;
    }
}
