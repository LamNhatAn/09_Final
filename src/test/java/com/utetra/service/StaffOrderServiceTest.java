package com.utetra.service;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.Branch;
import com.utetra.entity.User;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;
import com.utetra.event.OrderNotificationEvent;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class StaffOrderServiceTest {

    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository historyRepository;
    private ApplicationEventPublisher eventPublisher;
    private StaffOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        historyRepository = mock(OrderStatusHistoryRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new StaffOrderService(orderRepository, historyRepository, eventPublisher);
    }

    @Test
    void listAlwaysScopesOrdersToStaffBranch() {
        Page<Order> expected = new PageImpl<>(List.of(order(OrderStatus.PREPARING)));
        when(orderRepository.searchStaffManagement(eq(3L), eq(OrderStatus.PREPARING), eq("%090%"),
                eq(PaymentMethod.VNPAY), eq(PaymentStatus.PAID), any(Pageable.class)))
                .thenReturn(expected);

        assertEquals(expected, service.list(3L, OrderStatus.PREPARING, "090", 1));
        verify(orderRepository).searchStaffManagement(eq(3L), eq(OrderStatus.PREPARING), eq("%090%"),
                eq(PaymentMethod.VNPAY), eq(PaymentStatus.PAID), any(Pageable.class));
    }

    @Test
    void listRejectsStaffWithoutBranch() {
        assertThrows(StaffOrderException.class, () -> service.list(null, null, "", 1));
        verify(orderRepository, never()).searchStaffManagement(any(Long.class), any(), any(), any(), any(), any());
    }

    @Test
    void detailUsesCodeAndBranchTogether() {
        Order order = order(OrderStatus.CONFIRMED);
        OrderStatusHistory history = new OrderStatusHistory();
        when(orderRepository.findDetailedByCodeAndBranchId("UTE-1", 3L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(10L)).thenReturn(List.of(history));

        AdminOrderDetail detail = service.detail(3L, "UTE-1");

        assertEquals(order, detail.order());
        assertEquals(List.of(history), detail.history());
    }

    @Test
    void detailRejectsOrderOutsideStaffBranch() {
        when(orderRepository.findDetailedByCodeAndBranchId("UTE-1", 3L)).thenReturn(Optional.empty());
        assertThrows(StaffOrderException.class, () -> service.detail(3L, "UTE-1"));
    }

    @Test
    void advanceUsesBranchLockAndWritesStaffHistory() {
        Order order = order(OrderStatus.CONFIRMED);
        when(orderRepository.findByCodeAndBranchIdForUpdate("UTE-1", 3L)).thenReturn(Optional.of(order));

        assertEquals(OrderStatus.PREPARING, service.advance(88L, 3L, "UTE-1"));

        assertEquals(OrderStatus.PREPARING, order.getStatus());
        verify(orderRepository).save(order);
        ArgumentCaptor<OrderStatusHistory> captor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(OrderStatus.PREPARING, captor.getValue().getStatus());
        assertEquals(88L, captor.getValue().getChangedBy());
        verify(eventPublisher).publishEvent(any(OrderNotificationEvent.class));
    }

    @Test
    void advanceRejectsOrderOutsideStaffBranch() {
        when(orderRepository.findByCodeAndBranchIdForUpdate("UTE-1", 3L)).thenReturn(Optional.empty());
        assertThrows(StaffOrderException.class, () -> service.advance(88L, 3L, "UTE-1"));
        verify(historyRepository, never()).save(any());
    }

    @Test
    void advanceRejectsCompletedAndCancelledOrders() {
        for (OrderStatus terminal : List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            Order order = order(terminal);
            when(orderRepository.findByCodeAndBranchIdForUpdate("UTE-1", 3L)).thenReturn(Optional.of(order));
            assertThrows(StaffOrderException.class, () -> service.advance(88L, 3L, "UTE-1"));
        }
        verify(orderRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void acceptForPreparationMovesPendingOrderAndWritesBothAuditSteps() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findByCodeAndBranchIdForUpdate("UTE-1", 3L)).thenReturn(Optional.of(order));

        assertEquals(OrderStatus.PREPARING, service.acceptForPreparation(88L, 3L, "UTE-1"));

        assertEquals(OrderStatus.PREPARING, order.getStatus());
        verify(orderRepository).save(order);
        ArgumentCaptor<OrderStatusHistory> captor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(historyRepository, times(2)).save(captor.capture());
        assertEquals(List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING),
                captor.getAllValues().stream().map(OrderStatusHistory::getStatus).toList());
        verify(eventPublisher).publishEvent(any(OrderNotificationEvent.class));
    }

    @Test
    void acceptForPreparationRejectsOrderAlreadyBeingProcessed() {
        Order order = order(OrderStatus.PREPARING);
        when(orderRepository.findByCodeAndBranchIdForUpdate("UTE-1", 3L)).thenReturn(Optional.of(order));

        assertThrows(StaffOrderException.class, () -> service.acceptForPreparation(88L, 3L, "UTE-1"));

        verify(orderRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    private static Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(10L);
        order.setCode("UTE-1");
        order.setStatus(status);
        order.setPaymentMethod(PaymentMethod.COD);
        Branch branch = new Branch();
        branch.setId(3L);
        order.setBranch(branch);
        User user = new User();
        user.setId(7L);
        order.setUser(user);
        return order;
    }
}
