package com.utetra.service;

import com.utetra.dto.AdminDashboardStats;
import com.utetra.entity.Order;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.Role;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.FeedbackRepository;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.ProductRepository;
import com.utetra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional(readOnly = true)
    public AdminDashboardStats stats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return new AdminDashboardStats(
                userRepository.countByRole(Role.USER),
                userRepository.countByRoleAndActiveTrue(Role.STAFF),
                branchRepository.countByActiveTrue(),
                productRepository.countByAvailableTrueAndSoldOutFalse(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatusIn(List.of(
                        OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.DELIVERING)),
                orderRepository.countCompletedBetween(OrderStatus.COMPLETED, start, end),
                amount(orderRepository.sumTotalByStatusBetween(OrderStatus.COMPLETED, start, end)),
                amount(orderRepository.sumTotalByStatus(OrderStatus.COMPLETED)),
                feedbackRepository.count());
    }

    @Transactional(readOnly = true)
    public List<Order> recentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }

    private static BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
