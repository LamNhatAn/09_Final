package com.utetra.service;

import com.utetra.dto.CustomerOrderDetail;
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
import org.springframework.data.domain.Sort;

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

class CustomerOrderServiceTest {

    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository historyRepository;
    private CustomerOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        historyRepository = mock(OrderStatusHistoryRepository.class);
        service = new CustomerOrderService(orderRepository, historyRepository);
    }

    @Test
    void listOnlyUsesAuthenticatedUserAndNewestSort() {
        Page<Order> expected = new PageImpl<>(List.of(order(OrderStatus.PENDING)));
        when(orderRepository.findByUserId(any(Long.class), any(Pageable.class))).thenReturn(expected);

        Page<Order> result = service.list(7L, 1);

        assertEquals(expected, result);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findByUserId(org.mockito.ArgumentMatchers.eq(7L), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(8, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listPastLastPageFallsBackToLastPage() {
        Page<Order> pastLast = mock(Page.class);
        Page<Order> lastPage = new PageImpl<>(List.of(order(OrderStatus.COMPLETED)));
        when(pastLast.isEmpty()).thenReturn(true);
        when(pastLast.getTotalPages()).thenReturn(3);
        when(orderRepository.findByUserId(any(Long.class), any(Pageable.class)))
                .thenReturn(pastLast, lastPage);

        assertEquals(lastPage, service.list(7L, 99));
        verify(orderRepository, times(2)).findByUserId(any(Long.class), any(Pageable.class));
    }

    @Test
    void detailLoadsOwnedOrderAndHistory() {
        Order order = order(OrderStatus.CONFIRMED);
        OrderStatusHistory history = new OrderStatusHistory();
        when(orderRepository.findDetailedByCodeAndUserId("UTE-1", 7L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(10L)).thenReturn(List.of(history));

        CustomerOrderDetail result = service.detail(7L, "UTE-1");

        assertEquals(order, result.order());
        assertEquals(List.of(history), result.history());
        assertFalse(result.isCancellable());
    }

    @Test
    void detailRejectsAnotherUsersOrder() {
        when(orderRepository.findDetailedByCodeAndUserId("UTE-1", 8L)).thenReturn(Optional.empty());

        assertThrows(CustomerOrderException.class, () -> service.detail(8L, "UTE-1"));
        verify(historyRepository, never()).findByOrderIdOrderByChangedAtAscIdAsc(any());
    }

    @Test
    void cancelPendingOrderAndWritesHistory() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findOwnedForUpdate("UTE-1", 7L)).thenReturn(Optional.of(order));

        service.cancel(7L, "UTE-1");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
        ArgumentCaptor<OrderStatusHistory> captor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(order, captor.getValue().getOrder());
        assertEquals(OrderStatus.CANCELLED, captor.getValue().getStatus());
        assertEquals(7L, captor.getValue().getChangedBy());
    }

    @Test
    void cancelRejectsOrderAfterConfirmation() {
        Order order = order(OrderStatus.CONFIRMED);
        when(orderRepository.findOwnedForUpdate("UTE-1", 7L)).thenReturn(Optional.of(order));

        assertThrows(CustomerOrderException.class, () -> service.cancel(7L, "UTE-1"));
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void statusProvidesVietnameseLabelAndBadge() {
        assertEquals("Chờ xác nhận", OrderStatus.PENDING.getLabel());
        assertEquals("text-bg-danger", OrderStatus.CANCELLED.getBadgeClass());
        assertTrue(new com.utetra.dto.CustomerOrderDetail(order(OrderStatus.PENDING), List.of()).isCancellable());
    }

    private static Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(10L);
        order.setCode("UTE-1");
        order.setStatus(status);
        return order;
    }
}
