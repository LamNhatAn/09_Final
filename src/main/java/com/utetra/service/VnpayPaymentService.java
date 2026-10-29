package com.utetra.service;

import com.utetra.dto.VnpayCallbackOutcome;
import com.utetra.dto.VnpayPaymentRequest;
import com.utetra.entity.Order;
import com.utetra.entity.Payment;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;
import com.utetra.event.OrderNotificationEvent;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class VnpayPaymentService {

    private static final DateTimeFormatter REFERENCE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayGateway gateway;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VnpayPaymentService(VnpayGateway gateway, PaymentRepository paymentRepository,
                               OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.gateway = gateway;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public VnpayCallbackOutcome processCallback(Map<String, String> parameters) {
        if (!gateway.hasValidSignature(parameters) || !gateway.hasExpectedMerchant(parameters)) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.INVALID_SIGNATURE, null, null);
        }

        String txnRef = parameters.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.INVALID_REQUEST, null, null);
        }

        Payment payment = paymentRepository.findByVnpTxnRefForUpdate(txnRef).orElse(null);
        if (payment == null) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.NOT_FOUND, null, null);
        }
        Order order = payment.getOrder();

        long callbackAmount;
        try {
            callbackAmount = Long.parseLong(parameters.get("vnp_Amount"));
        } catch (RuntimeException e) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.INVALID_REQUEST,
                    order.getCode(), order.getPaymentStatus());
        }
        if (payment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact() != callbackAmount) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.INVALID_AMOUNT,
                    order.getCode(), order.getPaymentStatus());
        }
        if (payment.getStatus() != PaymentStatus.UNPAID) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.ALREADY_PROCESSED,
                    order.getCode(), payment.getStatus());
        }

        String responseCode = parameters.get("vnp_ResponseCode");
        String transactionStatus = parameters.get("vnp_TransactionStatus");
        if (responseCode == null || transactionStatus == null) {
            return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.INVALID_REQUEST,
                    order.getCode(), order.getPaymentStatus());
        }

        boolean paid = "00".equals(responseCode) && "00".equals(transactionStatus);
        payment.setVnpResponseCode(limit(responseCode, 10));
        payment.setVnpTransactionNo(limit(parameters.get("vnp_TransactionNo"), 50));
        payment.setStatus(paid ? PaymentStatus.PAID : PaymentStatus.FAILED);
        if (paid) {
            payment.setPaidAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);
        } else if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
        if (paid) {
            eventPublisher.publishEvent(OrderNotificationEvent.newOrder(
                    order.getId(), order.getCode(), order.getBranch().getId(),
                    order.getUser().getId(), PaymentMethod.VNPAY));
        }
        return new VnpayCallbackOutcome(VnpayCallbackOutcome.Type.PROCESSED,
                order.getCode(), payment.getStatus());
    }

    @Transactional
    public VnpayPaymentRequest retry(Long userId, String orderCode) {
        Order order = orderRepository.findOwnedForUpdate(orderCode, userId)
                .orElseThrow(() -> new VnpayException("Không tìm thấy đơn hàng"));
        if (order.getPaymentMethod() != PaymentMethod.VNPAY
                || order.getPaymentStatus() == PaymentStatus.PAID
                || order.getStatus() != OrderStatus.PENDING) {
            throw new VnpayException("Đơn hàng không thể thanh toán lại");
        }

        order.setPaymentStatus(PaymentStatus.UNPAID);
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(PaymentMethod.VNPAY);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.UNPAID);
        payment.setVnpTxnRef(generateReference());
        paymentRepository.save(payment);
        orderRepository.save(order);
        return new VnpayPaymentRequest(payment.getVnpTxnRef(), order.getCode(), payment.getAmount());
    }

    private static String generateReference() {
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "VNP-" + LocalDateTime.now().format(REFERENCE_TIME) + "-" + random;
    }

    private static String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
