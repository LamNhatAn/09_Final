package com.utetra.service;

import com.utetra.entity.PasswordResetOtp;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.PasswordResetOtpRepository;
import com.utetra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class PasswordResetService {

    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationOtpMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expiryMinutes;
    private final long resendSeconds;
    private final int maxAttempts;

    public PasswordResetService(PasswordResetOtpRepository otpRepository, UserRepository userRepository,
                                PasswordEncoder passwordEncoder, RegistrationOtpMailSender mailSender,
                                @Value("${app.password-reset-otp.expiry-minutes:10}") long expiryMinutes,
                                @Value("${app.password-reset-otp.resend-seconds:60}") long resendSeconds,
                                @Value("${app.password-reset-otp.max-attempts:5}") int maxAttempts) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.expiryMinutes = Math.max(1, expiryMinutes);
        this.resendSeconds = Math.max(30, resendSeconds);
        this.maxAttempts = Math.max(3, maxAttempts);
    }

    /** Luôn trả cùng kết quả để không làm lộ email nào đã đăng ký. */
    @Transactional
    public String start(String rawEmail) {
        String email = normalize(rawEmail);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getRole() != Role.USER || !user.isActive()) {
            return email;
        }
        LocalDateTime now = LocalDateTime.now();
        PasswordResetOtp pending = otpRepository.findLockedByEmail(email).orElse(null);
        if (pending != null && pending.getResendAvailableAt().isAfter(now)) {
            return email;
        }
        if (pending == null) {
            pending = new PasswordResetOtp();
            pending.setEmail(email);
        }
        String code = newCode();
        updateCode(pending, code, now);
        otpRepository.saveAndFlush(pending);
        mailSender.sendPasswordReset(email, code, expiryMinutes);
        return email;
    }

    @Transactional(noRollbackFor = InvalidOtpCodeException.class)
    public void reset(String rawEmail, String code, String newPassword) {
        String email = normalize(rawEmail);
        PasswordResetOtp pending = otpRepository.findLockedByEmail(email)
                .orElseThrow(() -> new PasswordResetException("Yêu cầu đổi mật khẩu không tồn tại hoặc đã hết hạn."));
        LocalDateTime now = LocalDateTime.now();
        if (pending.getExpiresAt().isBefore(now)) {
            throw new PasswordResetException("Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
        }
        if (pending.getAttempts() >= maxAttempts) {
            throw new PasswordResetException("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại mã OTP mới.");
        }
        if (code == null || !passwordEncoder.matches(code, pending.getOtpHash())) {
            pending.setAttempts(pending.getAttempts() + 1);
            otpRepository.save(pending);
            throw new InvalidOtpCodeException("Mã OTP không đúng. Bạn còn "
                    + Math.max(0, maxAttempts - pending.getAttempts()) + " lần thử.");
        }
        User user = userRepository.findByEmail(email)
                .filter(value -> value.getRole() == Role.USER && value.isActive())
                .orElseThrow(() -> new PasswordResetException("Tài khoản không còn khả dụng."));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpRepository.delete(pending);
    }

    @Transactional
    public void resend(String rawEmail) {
        String email = normalize(rawEmail);
        PasswordResetOtp pending = otpRepository.findLockedByEmail(email)
                .orElseThrow(() -> new PasswordResetException("Yêu cầu đổi mật khẩu không tồn tại. Vui lòng bắt đầu lại."));
        LocalDateTime now = LocalDateTime.now();
        if (pending.getResendAvailableAt().isAfter(now)) {
            long wait = Math.max(1, Duration.between(now, pending.getResendAvailableAt()).toSeconds() + 1);
            throw new PasswordResetException("Vui lòng đợi " + wait + " giây trước khi gửi lại mã.");
        }
        String code = newCode();
        updateCode(pending, code, now);
        otpRepository.saveAndFlush(pending);
        mailSender.sendPasswordReset(email, code, expiryMinutes);
    }

    private void updateCode(PasswordResetOtp pending, String code, LocalDateTime now) {
        pending.setOtpHash(passwordEncoder.encode(code));
        pending.setAttempts(0);
        pending.setExpiresAt(now.plusMinutes(expiryMinutes));
        pending.setResendAvailableAt(now.plusSeconds(resendSeconds));
    }

    private String newCode() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
    }

    private static String normalize(String email) {
        if (email == null || email.isBlank()) {
            throw new PasswordResetException("Email không hợp lệ");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
