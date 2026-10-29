package com.utetra.service;

import com.utetra.dto.NotificationItem;
import com.utetra.entity.Notification;
import com.utetra.entity.User;
import com.utetra.entity.enums.NotificationType;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.Role;
import com.utetra.event.OrderNotificationEvent;
import com.utetra.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final UserRepository userRepository;
    private final NotificationWriter notificationWriter;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderNotificationListener(UserRepository userRepository, NotificationWriter notificationWriter,
                                     SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.notificationWriter = notificationWriter;
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderNotificationEvent event) {
        if (event.type() == OrderNotificationEvent.Type.NEW_ORDER) {
            notifyStaff(event);
        } else {
            notifyCustomer(event);
        }
    }

    private void notifyStaff(OrderNotificationEvent event) {
        List<User> staff = userRepository.findByRoleAndBranchIdAndActiveTrue(Role.STAFF, event.branchId());
        String payment = event.paymentMethod() == PaymentMethod.VNPAY ? "đã thanh toán VNPay" : "thanh toán COD";
        for (User recipient : staff) {
            createAndPush(recipient,
                    "NEW_ORDER:" + event.orderId(),
                    NotificationType.NEW_ORDER,
                    "Đơn hàng mới " + event.orderCode(),
                    "Chi nhánh có đơn mới " + payment + ".",
                    "/staff/orders/" + event.orderCode());
        }
    }

    private void notifyCustomer(OrderNotificationEvent event) {
        if (event.status() != com.utetra.entity.enums.OrderStatus.PREPARING
                && event.status() != com.utetra.entity.enums.OrderStatus.COMPLETED) {
            return;
        }
        boolean accepted = event.status() == com.utetra.entity.enums.OrderStatus.PREPARING;
        String title = accepted
                ? "UTETra đã nhận đơn " + event.orderCode() + " rồi nhé!"
                : "Đơn " + event.orderCode() + " đã tới rồi nhé!";
        String message = accepted
                ? "Cửa hàng đang chuẩn bị món cho bạn. Cảm ơn bạn đã kiên nhẫn chờ một chút nhé."
                : "Món ngon đã được giao tới bạn. Chúc bạn thưởng thức thật vui vẻ!";
        userRepository.findById(event.customerId()).ifPresent(customer -> createAndPush(
                customer,
                "ORDER_CUSTOMER:" + event.orderId() + ":" + event.status().name(),
                NotificationType.ORDER_STATUS,
                title,
                message,
                "/orders/" + event.orderCode()));
    }

    private void createAndPush(User recipient, String eventKey, NotificationType type,
                               String title, String message, String link) {
        try {
            Notification saved = notificationWriter.createIfAbsent(
                    recipient, eventKey, type, title, message, link);
            if (saved != null) {
                messagingTemplate.convertAndSendToUser(
                        recipient.getEmail(), "/queue/notifications", NotificationItem.from(saved));
            }
        } catch (DataIntegrityViolationException e) {
            // Hai callback đồng thời: unique(user_id, event_key) giữ đúng một thông báo.
            log.debug("Notification already exists: user={}, event={}", recipient.getId(), eventKey);
        }
    }
}
