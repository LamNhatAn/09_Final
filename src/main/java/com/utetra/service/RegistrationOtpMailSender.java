package com.utetra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class RegistrationOtpMailSender {

    private final JavaMailSender mailSender;
    private final String host;
    private final String username;
    private final String configuredFrom;

    public RegistrationOtpMailSender(JavaMailSender mailSender,
                                     @Value("${spring.mail.host:}") String host,
                                     @Value("${spring.mail.username:}") String username,
                                     @Value("${app.mail.from:}") String configuredFrom) {
        this.mailSender = mailSender;
        this.host = host;
        this.username = username;
        this.configuredFrom = configuredFrom;
    }

    public void send(String recipient, String code, long expiryMinutes) {
        sendMessage(recipient, "Mã OTP xác minh tài khoản UTETra",
                "Mã OTP đăng ký UTETra của bạn là: " + code + "\n\n"
                        + "Mã có hiệu lực trong " + expiryMinutes + " phút. "
                        + "Không cung cấp mã này cho bất kỳ ai.\n\n"
                        + "Nếu bạn không yêu cầu đăng ký, hãy bỏ qua email này.");
    }

    public void sendPasswordReset(String recipient, String code, long expiryMinutes) {
        sendMessage(recipient, "Mã OTP đổi mật khẩu UTETra",
                "Mã OTP để đổi mật khẩu UTETra của bạn là: " + code + "\n\n"
                        + "Mã có hiệu lực trong " + expiryMinutes + " phút. "
                        + "Không cung cấp mã này cho bất kỳ ai.\n\n"
                        + "Nếu bạn không yêu cầu đổi mật khẩu, hãy bỏ qua email này.");
    }

    private void sendMessage(String recipient, String subject, String body) {
        String from = configuredFrom == null || configuredFrom.isBlank() ? username : configuredFrom;
        if (host == null || host.isBlank() || "localhost".equalsIgnoreCase(host)
                || username == null || username.isBlank() || from == null || from.isBlank()) {
            throw new EmailDeliveryException("Máy chủ chưa được cấu hình email gửi OTP");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailDeliveryException("Không gửi được email OTP", e);
        }
    }
}
