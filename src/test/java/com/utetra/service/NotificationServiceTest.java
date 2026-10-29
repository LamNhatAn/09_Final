package com.utetra.service;

import com.utetra.entity.Notification;
import com.utetra.entity.User;
import com.utetra.entity.enums.NotificationType;
import com.utetra.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository repository;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        service = new NotificationService(repository);
    }

    @Test
    void feedOnlyLoadsCurrentUsersNotificationsAndUnreadCount() {
        Notification notification = notification(5L, 7L);
        when(repository.findTop20ByUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(notification));
        when(repository.countByUserIdAndReadAtIsNull(7L)).thenReturn(3L);

        var feed = service.feed(7L);

        assertEquals(3L, feed.unreadCount());
        assertEquals(1, feed.items().size());
        assertEquals(5L, feed.items().get(0).id());
    }

    @Test
    void markReadChecksOwnership() {
        when(repository.findByIdAndUserId(5L, 7L)).thenReturn(Optional.of(notification(5L, 7L)));

        assertTrue(service.markRead(7L, 5L));
        verify(repository).save(any(Notification.class));

        assertFalse(service.markRead(8L, 5L));
        verify(repository).findByIdAndUserId(5L, 8L);
    }

    @Test
    void markAllReadIsAlwaysScopedToCurrentUser() {
        when(repository.markAllRead(eq(7L), any())).thenReturn(4);
        assertEquals(4, service.markAllRead(7L));
        verify(repository).markAllRead(eq(7L), any());
    }

    private static Notification notification(Long id, Long userId) {
        User user = new User();
        user.setId(userId);
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUser(user);
        notification.setType(NotificationType.NEW_ORDER);
        notification.setTitle("Đơn mới");
        notification.setMessage("Có đơn mới");
        notification.setLink("/staff/orders/UTE-1");
        return notification;
    }
}
