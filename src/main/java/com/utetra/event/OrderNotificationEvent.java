package com.utetra.event;

import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;

public record OrderNotificationEvent(Type type, Long orderId, String orderCode, Long branchId,
                                     Long customerId, OrderStatus status, PaymentMethod paymentMethod) {

    public enum Type {
        NEW_ORDER,
        STATUS_CHANGED
    }

    public static OrderNotificationEvent newOrder(Long orderId, String code, Long branchId,
                                                  Long customerId, PaymentMethod paymentMethod) {
        return new OrderNotificationEvent(Type.NEW_ORDER, orderId, code, branchId,
                customerId, OrderStatus.PENDING, paymentMethod);
    }

    public static OrderNotificationEvent statusChanged(Long orderId, String code, Long branchId,
                                                       Long customerId, OrderStatus status,
                                                       PaymentMethod paymentMethod) {
        return new OrderNotificationEvent(Type.STATUS_CHANGED, orderId, code, branchId,
                customerId, status, paymentMethod);
    }
}
