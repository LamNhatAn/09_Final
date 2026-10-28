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
public class StaffOrderService {

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public Page<Order> list(Long branchId, OrderStatus status, String keyword, int page) {
        requireBranch(branchId);
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sort);
        String pattern = AdminOrderService.searchPattern(keyword);
        Page<Order> result = orderRepository.searchStaffManagement(branchId, status, pattern,
                PaymentMethod.VNPAY, PaymentStatus.PAID, pageable);
        if (result.isEmpty() && result.getTotalPages() > 0
                && pageable.getPageNumber() >= result.getTotalPages()) {
            result = orderRepository.searchStaffManagement(branchId, status, pattern,
                    PaymentMethod.VNPAY, PaymentStatus.PAID,
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AdminOrderDetail detail(Long branchId, String code) {
        requireBranch(branchId);
        Order order = orderRepository.findDetailedByCodeAndBranchId(code, branchId)
                .orElseThrow(() -> new StaffOrderException("Không tìm thấy đơn hàng của chi nhánh"));
        return new AdminOrderDetail(order,
                historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(order.getId()));
    }

    @Transactional
    public OrderStatus advance(Long staffId, Long branchId, String code) {
        requireBranch(branchId);
        Order order = orderRepository.findByCodeAndBranchIdForUpdate(code, branchId)
                .orElseThrow(() -> new StaffOrderException("Không tìm thấy đơn hàng của chi nhánh"));
        OrderStatus next = order.getStatus().next();
        if (next == null || (order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.PAID)) {
            throw new StaffOrderException("Đơn hàng đã kết thúc, không thể chuyển tiếp");
        }

        order.setStatus(next);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(next);
        history.setChangedBy(staffId);
        historyRepository.save(history);
        return next;
    }

    private static void requireBranch(Long branchId) {
        if (branchId == null) {
            throw new StaffOrderException("Tài khoản nhân viên chưa được gán chi nhánh");
        }
    }
}
