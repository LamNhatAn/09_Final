package com.utetra.service;

import com.utetra.dto.NotificationFeed;
import com.utetra.dto.NotificationItem;
import com.utetra.entity.Notification;
import com.utetra.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public NotificationFeed feed(Long userId) {
        var items = notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationItem::from)
                .toList();
        return new NotificationFeed(notificationRepository.countByUserIdAndReadAtIsNull(userId), items);
    }

    @Transactional
    public boolean markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElse(null);
        if (notification == null) {
            return false;
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return true;
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId, LocalDateTime.now());
    }
}
