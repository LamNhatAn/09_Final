package com.utetra.service;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;
import com.utetra.event.OrderNotificationEvent;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.OrderStatusHistoryRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffOrderService {

    private static final int PAGE_SIZE = 10;
    private static final int WORK_QUEUE_SIZE = 50;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public StaffOrderService(OrderRepository orderRepository, OrderStatusHistoryRepository historyRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.eventPublisher = eventPublisher;
    }

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
    public List<Order> workQueue(Long branchId, OrderStatus status, String keyword) {
        requireBranch(branchId);
        Sort sort = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        return orderRepository.searchStaffManagement(branchId, status,
                AdminOrderService.searchPattern(keyword), PaymentMethod.VNPAY, PaymentStatus.PAID,
                PageRequest.of(0, WORK_QUEUE_SIZE, sort)).getContent();
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
        saveHistory(order, next, staffId);
        eventPublisher.publishEvent(OrderNotificationEvent.statusChanged(
                order.getId(), order.getCode(), order.getBranch().getId(), order.getUser().getId(),
                next, order.getPaymentMethod()));
        return next;
    }

    @Transactional
    public OrderStatus acceptForPreparation(Long staffId, Long branchId, String code) {
        requireBranch(branchId);
        Order order = orderRepository.findByCodeAndBranchIdForUpdate(code, branchId)
                .orElseThrow(() -> new StaffOrderException("Không tìm thấy đơn hàng của chi nhánh"));
        if (order.getStatus() != OrderStatus.PENDING || (order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.PAID)) {
            throw new StaffOrderException("Đơn hàng không còn ở hàng chờ xác nhận");
        }

        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);
        saveHistory(order, OrderStatus.CONFIRMED, staffId);
        saveHistory(order, OrderStatus.PREPARING, staffId);
        eventPublisher.publishEvent(OrderNotificationEvent.statusChanged(
                order.getId(), order.getCode(), order.getBranch().getId(), order.getUser().getId(),
                OrderStatus.PREPARING, order.getPaymentMethod()));
        return OrderStatus.PREPARING;
    }

    private void saveHistory(Order order, OrderStatus status, Long staffId) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setChangedBy(staffId);
        historyRepository.save(history);
    }

    private static void requireBranch(Long branchId) {
        if (branchId == null) {
            throw new StaffOrderException("Tài khoản nhân viên chưa được gán chi nhánh");
        }
    }
}
