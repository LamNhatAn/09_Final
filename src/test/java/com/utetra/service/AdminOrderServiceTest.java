package com.utetra.service;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.entity.Order;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminOrderServiceTest {

    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository historyRepository;
    private AdminOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        historyRepository = mock(OrderStatusHistoryRepository.class);
        service = new AdminOrderService(orderRepository, historyRepository);
    }

    @Test
    void listReturnsAllOrdersNewestFirst() {
        Page<Order> expected = new PageImpl<>(List.of(order(OrderStatus.PENDING)));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(expected);

        Page<Order> result = service.list(1);

        assertEquals(expected, result);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listPastLastPageFallsBackToLastPage() {
        Page<Order> pastLast = mock(Page.class);
        Page<Order> lastPage = new PageImpl<>(List.of(order(OrderStatus.CONFIRMED)));
        when(pastLast.isEmpty()).thenReturn(true);
        when(pastLast.getTotalPages()).thenReturn(2);
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(pastLast, lastPage);

        assertEquals(lastPage, service.list(99));
        verify(orderRepository, times(2)).findAll(any(Pageable.class));
    }

    @Test
    void detailLoadsOrderAndHistory() {
        Order order = order(OrderStatus.PENDING);
        OrderStatusHistory history = new OrderStatusHistory();
        when(orderRepository.findDetailedByCode("UTE-1")).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(10L)).thenReturn(List.of(history));

        AdminOrderDetail detail = service.detail("UTE-1");

        assertEquals(order, detail.order());
        assertEquals(List.of(history), detail.history());
        assertTrue(detail.isConfirmable());
    }

    @Test
    void confirmPendingOrderAndWritesHistory() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findByCodeForUpdate("UTE-1")).thenReturn(Optional.of(order));

        service.confirm(99L, "UTE-1");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
        ArgumentCaptor<OrderStatusHistory> captor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(OrderStatus.CONFIRMED, captor.getValue().getStatus());
        assertEquals(99L, captor.getValue().getChangedBy());
    }

    @Test
    void confirmRejectsOrderThatIsNotPending() {
        Order order = order(OrderStatus.CANCELLED);
        when(orderRepository.findByCodeForUpdate("UTE-1")).thenReturn(Optional.of(order));

        assertThrows(AdminOrderException.class, () -> service.confirm(99L, "UTE-1"));
        assertFalse(new AdminOrderDetail(order, List.of()).isConfirmable());
        verify(orderRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void confirmRejectsUnknownOrder() {
        when(orderRepository.findByCodeForUpdate("MISSING")).thenReturn(Optional.empty());

        assertThrows(AdminOrderException.class, () -> service.confirm(99L, "MISSING"));
        verify(historyRepository, never()).save(any());
    }

    private static Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(10L);
        order.setCode("UTE-1");
        order.setStatus(status);
        return order;
    }
}
