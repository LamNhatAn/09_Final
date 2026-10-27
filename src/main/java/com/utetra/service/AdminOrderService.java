package com.utetra.service;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.enums.OrderStatus;
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
public class AdminOrderService {

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public Page<Order> list(int page) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sort);
        Page<Order> result = orderRepository.findAll(pageable);
        if (result.isEmpty() && result.getTotalPages() > 0
                && pageable.getPageNumber() >= result.getTotalPages()) {
            result = orderRepository.findAll(
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AdminOrderDetail detail(String code) {
        Order order = orderRepository.findDetailedByCode(code)
                .orElseThrow(() -> new AdminOrderException("Không tìm thấy đơn hàng"));
        return new AdminOrderDetail(order,
                historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(order.getId()));
    }

    @Transactional
    public void confirm(Long adminId, String code) {
        Order order = orderRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new AdminOrderException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AdminOrderException("Chỉ có thể xác nhận đơn đang chờ xác nhận");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(OrderStatus.CONFIRMED);
        history.setChangedBy(adminId);
        historyRepository.save(history);
    }
}
