package com.utetra.service;

import com.utetra.config.VnpayProperties;
import com.utetra.dto.VnpayPaymentRequest;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VnpayGateway {

    private static final DateTimeFormatter VNPAY_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final VnpayProperties properties;
    private Clock clock = Clock.system(VIETNAM_ZONE);

    public VnpayGateway(VnpayProperties properties) {
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String createPaymentUrl(VnpayPaymentRequest payment, String clientIp) {
        requireConfigured();
        LocalDateTime now = LocalDateTime.now(clock);
        Map<String, String> fields = new TreeMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TmnCode", properties.getTmnCode().trim());
        fields.put("vnp_Amount", payment.amount().longValueExact() * 100 + "");
        fields.put("vnp_CurrCode", "VND");
        fields.put("vnp_TxnRef", payment.transactionRef());
        fields.put("vnp_OrderInfo", "Thanh toan don hang " + payment.orderCode());
        fields.put("vnp_OrderType", "other");
        fields.put("vnp_Locale", "vn");
        fields.put("vnp_ReturnUrl", properties.getReturnUrl());
        fields.put("vnp_IpAddr", normalizeIp(clientIp));
        fields.put("vnp_CreateDate", now.format(VNPAY_TIME));
        fields.put("vnp_ExpireDate", now.plusMinutes(Math.max(properties.getExpireMinutes(), 1)).format(VNPAY_TIME));

        String query = canonical(fields);
        return properties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + hmacSha512(query);
    }

    public boolean hasValidSignature(Map<String, String> parameters) {
        if (!isConfigured() || parameters == null) {
            return false;
        }
        String supplied = parameters.get("vnp_SecureHash");
        if (supplied == null || supplied.isBlank()) {
            return false;
        }
        Map<String, String> signedFields = new TreeMap<>();
        parameters.forEach((key, value) -> {
            if (key != null && key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)
                    && value != null && !value.isEmpty()) {
                signedFields.put(key, value);
            }
        });
        String expected = hmacSha512(canonical(signedFields));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                supplied.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    public boolean hasExpectedMerchant(Map<String, String> parameters) {
        return isConfigured() && properties.getTmnCode().trim().equals(parameters.get("vnp_TmnCode"));
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new VnpayException("Chưa cấu hình mã website và khóa bí mật VNPay");
        }
    }

    private String hmacSha512(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(properties.getHashSecret().trim().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception e) {
            throw new VnpayException("Không thể ký yêu cầu VNPay");
        }
    }

    private static String canonical(Map<String, String> fields) {
        return fields.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "127.0.0.1";
        }
        String value = clientIp.trim();
        return value.length() <= 45 ? value : value.substring(0, 45);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }
}
