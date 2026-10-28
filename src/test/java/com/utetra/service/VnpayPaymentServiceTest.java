package com.utetra.service;

import com.utetra.dto.VnpayCallbackOutcome;
import com.utetra.entity.Order;
import com.utetra.entity.Payment;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VnpayPaymentServiceTest {

    private VnpayGateway gateway;
    private PaymentRepository paymentRepository;
    private OrderRepository orderRepository;
    private VnpayPaymentService service;

    @BeforeEach
    void setUp() {
        gateway = mock(VnpayGateway.class);
        paymentRepository = mock(PaymentRepository.class);
        orderRepository = mock(OrderRepository.class);
        service = new VnpayPaymentService(gateway, paymentRepository, orderRepository);
        when(gateway.hasValidSignature(anyMap())).thenReturn(true);
        when(gateway.hasExpectedMerchant(anyMap())).thenReturn(true);
    }

    @Test
    void successfulCallbackMarksPaymentAndOrderPaid() {
        Payment payment = payment(PaymentStatus.UNPAID);
        when(paymentRepository.findByVnpTxnRefForUpdate("VNP-1")).thenReturn(Optional.of(payment));

        VnpayCallbackOutcome result = service.processCallback(callback("00", "00", "3500000"));

        assertEquals(VnpayCallbackOutcome.Type.PROCESSED, result.getType());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(PaymentStatus.PAID, payment.getOrder().getPaymentStatus());
        assertEquals("998877", payment.getVnpTransactionNo());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(payment.getOrder());
    }

    @Test
    void failedCallbackRecordsFailureAndAllowsLaterRetry() {
        Payment payment = payment(PaymentStatus.UNPAID);
        when(paymentRepository.findByVnpTxnRefForUpdate("VNP-1")).thenReturn(Optional.of(payment));

        VnpayCallbackOutcome result = service.processCallback(callback("24", "02", "3500000"));

        assertEquals(VnpayCallbackOutcome.Type.PROCESSED, result.getType());
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(PaymentStatus.FAILED, payment.getOrder().getPaymentStatus());
    }

    @Test
    void duplicateCallbackDoesNotUpdateAgain() {
        Payment payment = payment(PaymentStatus.PAID);
        when(paymentRepository.findByVnpTxnRefForUpdate("VNP-1")).thenReturn(Optional.of(payment));

        VnpayCallbackOutcome result = service.processCallback(callback("00", "00", "3500000"));

        assertEquals(VnpayCallbackOutcome.Type.ALREADY_PROCESSED, result.getType());
        verify(paymentRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidSignatureBeforeReadingDatabase() {
        when(gateway.hasValidSignature(anyMap())).thenReturn(false);

        VnpayCallbackOutcome result = service.processCallback(callback("00", "00", "3500000"));

        assertEquals(VnpayCallbackOutcome.Type.INVALID_SIGNATURE, result.getType());
        verify(paymentRepository, never()).findByVnpTxnRefForUpdate(any());
    }

    @Test
    void rejectsAmountMismatchWithoutUpdating() {
        Payment payment = payment(PaymentStatus.UNPAID);
        when(paymentRepository.findByVnpTxnRefForUpdate("VNP-1")).thenReturn(Optional.of(payment));

        VnpayCallbackOutcome result = service.processCallback(callback("00", "00", "1"));

        assertEquals(VnpayCallbackOutcome.Type.INVALID_AMOUNT, result.getType());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void retryCreatesNewAttemptForOwnedUnpaidOrder() {
        Order order = order();
        order.setPaymentStatus(PaymentStatus.FAILED);
        when(orderRepository.findOwnedForUpdate("UTE-1", 7L)).thenReturn(Optional.of(order));

        var request = service.retry(7L, "UTE-1");

        assertEquals("UTE-1", request.orderCode());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(order);
    }

    @Test
    void retryRejectsAlreadyPaidOrder() {
        Order order = order();
        order.setPaymentStatus(PaymentStatus.PAID);
        when(orderRepository.findOwnedForUpdate("UTE-1", 7L)).thenReturn(Optional.of(order));

        assertThrows(VnpayException.class, () -> service.retry(7L, "UTE-1"));
        verify(paymentRepository, never()).save(any());
    }

    private static Payment payment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setOrder(order());
        payment.setMethod(PaymentMethod.VNPAY);
        payment.setAmount(new BigDecimal("35000"));
        payment.setStatus(status);
        payment.setVnpTxnRef("VNP-1");
        return payment;
    }

    private static Order order() {
        Order order = new Order();
        order.setId(10L);
        order.setCode("UTE-1");
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.VNPAY);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setTotalAmount(new BigDecimal("35000"));
        return order;
    }

    private static Map<String, String> callback(String responseCode, String transactionStatus, String amount) {
        Map<String, String> values = new HashMap<>();
        values.put("vnp_TxnRef", "VNP-1");
        values.put("vnp_Amount", amount);
        values.put("vnp_ResponseCode", responseCode);
        values.put("vnp_TransactionStatus", transactionStatus);
        values.put("vnp_TransactionNo", "998877");
        return values;
    }
}
