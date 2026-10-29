package com.utetra.service;

import com.utetra.entity.Notification;
import com.utetra.entity.User;
import com.utetra.entity.enums.NotificationType;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.Role;
import com.utetra.event.OrderNotificationEvent;
import com.utetra.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderNotificationListenerTest {

    private UserRepository userRepository;
    private NotificationWriter writer;
    private SimpMessagingTemplate messagingTemplate;
    private OrderNotificationListener listener;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        writer = mock(NotificationWriter.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        listener = new OrderNotificationListener(userRepository, writer, messagingTemplate);
    }

    @Test
    void newOrderOnlyTargetsActiveStaffOfOrdersBranch() {
        User staff = user(20L, "staff@utetra.vn");
        when(userRepository.findByRoleAndBranchIdAndActiveTrue(Role.STAFF, 3L)).thenReturn(List.of(staff));

        listener.handle(OrderNotificationEvent.newOrder(10L, "UTE-1", 3L, 7L, PaymentMethod.COD));

        verify(userRepository).findByRoleAndBranchIdAndActiveTrue(Role.STAFF, 3L);
        verify(writer).createIfAbsent(eq(staff), eq("NEW_ORDER:10"), eq(NotificationType.NEW_ORDER),
                contains("UTE-1"), contains("COD"), eq("/staff/orders/UTE-1"));
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void statusChangeOnlyTargetsOrderCustomerAndPushesRealtime() {
        User customer = user(7L, "user@utetra.vn");
        Notification saved = notification(customer);
        when(userRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(writer.createIfAbsent(any(), anyString(), any(), anyString(), anyString(), anyString()))
                .thenReturn(saved);

        listener.handle(OrderNotificationEvent.statusChanged(
                10L, "UTE-1", 3L, 7L, OrderStatus.PREPARING, PaymentMethod.COD));

        verify(userRepository).findById(7L);
        verify(writer).createIfAbsent(eq(customer), eq("ORDER_CUSTOMER:10:PREPARING"),
                eq(NotificationType.ORDER_STATUS), contains("UTE-1"), contains("chuẩn bị"),
                eq("/orders/UTE-1"));
        verify(messagingTemplate).convertAndSendToUser(eq("user@utetra.vn"),
                eq("/queue/notifications"), any());
    }

    @Test
    void intermediateStatusDoesNotNotifyCustomer() {
        User customer = user(7L, "user@utetra.vn");
        listener.handle(OrderNotificationEvent.statusChanged(
                10L, "UTE-1", 3L, 7L, OrderStatus.CONFIRMED, PaymentMethod.COD));

        verifyNoInteractions(userRepository, writer);
        verifyNoInteractions(messagingTemplate);
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setActive(true);
        return user;
    }

    private static Notification notification(User user) {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setType(NotificationType.ORDER_STATUS);
        notification.setTitle("Cập nhật đơn");
        notification.setMessage("Đang pha chế");
        notification.setLink("/orders/UTE-1");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
