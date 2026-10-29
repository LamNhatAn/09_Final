package com.utetra.service;

import com.utetra.dto.RegisterForm;
import com.utetra.entity.RegistrationOtp;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.RegistrationOtpRepository;
import com.utetra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class RegistrationOtpService {

    private final RegistrationOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationOtpMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expiryMinutes;
    private final long resendSeconds;
    private final int maxAttempts;

    public RegistrationOtpService(RegistrationOtpRepository otpRepository, UserRepository userRepository,
                                  PasswordEncoder passwordEncoder, RegistrationOtpMailSender mailSender,
                                  @Value("${app.registration-otp.expiry-minutes:10}") long expiryMinutes,
                                  @Value("${app.registration-otp.resend-seconds:60}") long resendSeconds,
                                  @Value("${app.registration-otp.max-attempts:5}") int maxAttempts) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.expiryMinutes = Math.max(1, expiryMinutes);
        this.resendSeconds = Math.max(30, resendSeconds);
        this.maxAttempts = Math.max(3, maxAttempts);
    }

    @Transactional
    public String start(RegisterForm form) {
        String email = normalizeEmail(form.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        LocalDateTime now = LocalDateTime.now();
        RegistrationOtp pending = otpRepository.findLockedByEmail(email).orElse(null);
        if (pending != null && pending.getResendAvailableAt().isAfter(now)) {
            return email;
        }
        if (pending == null) {
            pending = new RegistrationOtp();
            pending.setEmail(email);
        }

        String code = newCode();
        pending.setFullName(form.getFullName().trim());
        pending.setPhone(blankToNull(form.getPhone()));
        pending.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        pending.setOtpHash(passwordEncoder.encode(code));
        pending.setAttempts(0);
        pending.setExpiresAt(now.plusMinutes(expiryMinutes));
        pending.setResendAvailableAt(now.plusSeconds(resendSeconds));
        try {
            otpRepository.saveAndFlush(pending);
        } catch (DataIntegrityViolationException e) {
            throw new RegistrationOtpException("Đang có một yêu cầu xác minh cho email này. Vui lòng thử lại.");
        }
        mailSender.send(email, code, expiryMinutes);
        return email;
    }

    @Transactional(noRollbackFor = InvalidOtpCodeException.class)
    public User verify(String rawEmail, String code) {
        String email = normalizeEmail(rawEmail);
        RegistrationOtp pending = otpRepository.findLockedByEmail(email)
                .orElseThrow(() -> new RegistrationOtpException("Phiên xác minh không tồn tại. Vui lòng đăng ký lại."));
        LocalDateTime now = LocalDateTime.now();
        if (pending.getExpiresAt().isBefore(now)) {
            throw new RegistrationOtpException("Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
        }
        if (pending.getAttempts() >= maxAttempts) {
            throw new RegistrationOtpException("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại mã OTP mới.");
        }
        if (code == null || !passwordEncoder.matches(code, pending.getOtpHash())) {
            pending.setAttempts(pending.getAttempts() + 1);
            otpRepository.save(pending);
            int remaining = Math.max(0, maxAttempts - pending.getAttempts());
            throw new InvalidOtpCodeException("Mã OTP không đúng. Bạn còn " + remaining + " lần thử.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationOtpException("Email này đã được đăng ký. Hãy chuyển sang đăng nhập.");
        }

        User user = new User();
        user.setFullName(pending.getFullName());
        user.setEmail(email);
        user.setPhone(pending.getPhone());
        user.setPasswordHash(pending.getPasswordHash());
        user.setRole(Role.USER);
        user.setActive(true);
        try {
            User saved = userRepository.saveAndFlush(user);
            otpRepository.delete(pending);
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new RegistrationOtpException("Email này đã được đăng ký. Hãy chuyển sang đăng nhập.");
        }
    }

    @Transactional
    public void resend(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        RegistrationOtp pending = otpRepository.findLockedByEmail(email)
                .orElseThrow(() -> new RegistrationOtpException("Phiên xác minh không tồn tại. Vui lòng đăng ký lại."));
        LocalDateTime now = LocalDateTime.now();
        if (pending.getResendAvailableAt().isAfter(now)) {
            long wait = Math.max(1, Duration.between(now, pending.getResendAvailableAt()).toSeconds() + 1);
            throw new RegistrationOtpException("Vui lòng đợi " + wait + " giây trước khi gửi lại mã.");
        }
        if (userRepository.existsByEmail(email)) {
            otpRepository.delete(pending);
            throw new RegistrationOtpException("Email này đã được đăng ký. Hãy chuyển sang đăng nhập.");
        }

        String code = newCode();
        pending.setOtpHash(passwordEncoder.encode(code));
        pending.setAttempts(0);
        pending.setExpiresAt(now.plusMinutes(expiryMinutes));
        pending.setResendAvailableAt(now.plusSeconds(resendSeconds));
        otpRepository.saveAndFlush(pending);
        mailSender.send(email, code, expiryMinutes);
    }

    private String newCode() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RegistrationOtpException("Email không hợp lệ");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
