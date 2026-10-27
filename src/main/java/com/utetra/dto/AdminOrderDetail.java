package com.utetra.dto;

import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.enums.OrderStatus;

import java.util.List;

public record AdminOrderDetail(Order order, List<OrderStatusHistory> history) {

    public Order getOrder() {
        return order;
    }

    public List<OrderStatusHistory> getHistory() {
        return history;
    }

    public boolean isConfirmable() {
        return order.getStatus() == OrderStatus.PENDING;
    }
}
