package com.utetra.service;

import com.utetra.dto.AdminOrderDetail;
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
public class AdminOrderService {

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public Page<Order> list(Long branchId, OrderStatus status, String keyword, int page) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sort);
        long selectedBranch = branchId == null ? 0L : branchId;
        String pattern = searchPattern(keyword);
        Page<Order> result = orderRepository.searchManagement(selectedBranch, status, pattern, pageable);
        if (result.isEmpty() && result.getTotalPages() > 0
                && pageable.getPageNumber() >= result.getTotalPages()) {
            result = orderRepository.searchManagement(selectedBranch, status, pattern,
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
    public OrderStatus advance(Long adminId, String code) {
        Order order = orderRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new AdminOrderException("Không tìm thấy đơn hàng"));
        OrderStatus next = order.getStatus().next();
        if (next == null || (order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.PAID)) {
            throw new AdminOrderException("Đơn hàng đã kết thúc, không thể chuyển tiếp");
        }

        order.setStatus(next);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(next);
        history.setChangedBy(adminId);
        historyRepository.save(history);
        return next;
    }

    static String searchPattern(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        if (value.length() > 100) {
            value = value.substring(0, 100);
        }
        String escaped = value.replace("!", "!!").replace("%", "!%")
                .replace("_", "!_").replace("[", "![");
        return "%" + escaped + "%";
    }
}
