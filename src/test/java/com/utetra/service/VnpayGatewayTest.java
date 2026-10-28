package com.utetra.service;

import com.utetra.config.VnpayProperties;
import com.utetra.dto.VnpayPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnpayGatewayTest {

    private VnpayGateway gateway;

    @BeforeEach
    void setUp() {
        VnpayProperties properties = new VnpayProperties();
        properties.setTmnCode("TESTCODE");
        properties.setHashSecret("test-secret-value");
        properties.setReturnUrl("http://localhost:8080/payments/vnpay/return");
        gateway = new VnpayGateway(properties);
        gateway.setClock(Clock.fixed(Instant.parse("2026-10-28T03:15:30Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    @Test
    void createsSignedVersion210PaymentUrl() {
        String url = gateway.createPaymentUrl(
                new VnpayPaymentRequest("VNP-123", "UTE-123", new BigDecimal("35000")), "127.0.0.1");
        Map<String, String> parameters = parameters(url);

        assertEquals("2.1.0", parameters.get("vnp_Version"));
        assertEquals("3500000", parameters.get("vnp_Amount"));
        assertEquals("20261028101530", parameters.get("vnp_CreateDate"));
        assertEquals("20261028103030", parameters.get("vnp_ExpireDate"));
        assertTrue(gateway.hasValidSignature(parameters));
        assertTrue(gateway.hasExpectedMerchant(parameters));
    }

    @Test
    void rejectsTamperedSignedData() {
        Map<String, String> parameters = parameters(gateway.createPaymentUrl(
                new VnpayPaymentRequest("VNP-123", "UTE-123", new BigDecimal("35000")), "127.0.0.1"));
        parameters.put("vnp_Amount", "100");

        assertFalse(gateway.hasValidSignature(parameters));
    }

    private static Map<String, String> parameters(String url) {
        return Arrays.stream(URI.create(url).getRawQuery().split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(pair -> decode(pair[0]), pair -> decode(pair[1])));
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
