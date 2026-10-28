package com.utetra.service;

import com.utetra.dto.CustomerOrderDetail;
import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private static final int PAGE_SIZE = 8;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public Page<Order> list(Long userId, int page) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sort);
        Page<Order> result = orderRepository.findByUserId(userId, pageable);
        if (result.isEmpty() && result.getTotalPages() > 0
                && pageable.getPageNumber() >= result.getTotalPages()) {
            result = orderRepository.findByUserId(userId,
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public CustomerOrderDetail detail(Long userId, String code) {
        Order order = orderRepository.findDetailedByCodeAndUserId(code, userId)
                .orElseThrow(() -> new CustomerOrderException("Không tìm thấy đơn hàng"));
        return new CustomerOrderDetail(order,
                historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(order.getId()));
    }

    @Transactional
    public void cancel(Long userId, String code) {
        Order order = orderRepository.findOwnedForUpdate(code, userId)
                .orElseThrow(() -> new CustomerOrderException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PENDING
                || (order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.FAILED)) {
            throw new CustomerOrderException("Đơn hàng không thể hủy trực tiếp");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(OrderStatus.CANCELLED);
        history.setChangedBy(userId);
        historyRepository.save(history);
    }
}
