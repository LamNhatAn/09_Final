package com.utetra.service;

import com.utetra.entity.Notification;
import com.utetra.entity.User;
import com.utetra.entity.enums.NotificationType;
import com.utetra.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationWriter {

    private final NotificationRepository notificationRepository;

    public NotificationWriter(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** Mỗi event chỉ tạo một thông báo cho từng user, kể cả callback VNPay bị gửi lặp. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createIfAbsent(User user, String eventKey, NotificationType type,
                                       String title, String message, String link) {
        if (notificationRepository.existsByUserIdAndEventKey(user.getId(), eventKey)) {
            return null;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEventKey(eventKey);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        return notificationRepository.saveAndFlush(notification);
    }
}
