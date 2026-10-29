package com.utetra.dto;

import com.utetra.entity.Notification;
import com.utetra.entity.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationItem(Long id, NotificationType type, String title, String message,
                               String link, boolean unread, LocalDateTime createdAt) {

    public static NotificationItem from(Notification notification) {
        return new NotificationItem(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLink(),
                notification.getReadAt() == null,
                notification.getCreatedAt());
    }
}
