package com.utetra.service;

import com.utetra.entity.PasswordResetOtp;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.PasswordResetOtpRepository;
import com.utetra.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private PasswordResetOtpRepository otpRepository;
    private UserRepository userRepository;
    private PasswordEncoder encoder;
    private RegistrationOtpMailSender mailSender;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        otpRepository = mock(PasswordResetOtpRepository.class);
        userRepository = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        mailSender = mock(RegistrationOtpMailSender.class);
        service = new PasswordResetService(otpRepository, userRepository, encoder, mailSender, 10, 60, 5);
        when(encoder.encode(anyString())).thenReturn("encoded");
    }

    @Test
    void startSendsOtpOnlyForActiveCustomer() {
        User user = customer();
        when(userRepository.findByEmail("user@utetra.vn")).thenReturn(Optional.of(user));
        when(otpRepository.findLockedByEmail("user@utetra.vn")).thenReturn(Optional.empty());

        assertEquals("user@utetra.vn", service.start(" USER@utetra.vn "));

        verify(otpRepository).saveAndFlush(argThat(p -> p.getEmail().equals("user@utetra.vn")
                && p.getAttempts() == 0 && p.getExpiresAt() != null));
        verify(mailSender).sendPasswordReset(eq("user@utetra.vn"), matches("[0-9]{6}"), eq(10L));
    }

    @Test
    void unknownEmailDoesNotRevealAccountOrSendMail() {
        when(userRepository.findByEmail("none@utetra.vn")).thenReturn(Optional.empty());

        assertEquals("none@utetra.vn", service.start("none@utetra.vn"));

        verifyNoInteractions(otpRepository, mailSender);
    }

    @Test
    void validOtpChangesPasswordAndRemovesRequest() {
        PasswordResetOtp pending = pending();
        User user = customer();
        when(otpRepository.findLockedByEmail("user@utetra.vn")).thenReturn(Optional.of(pending));
        when(encoder.matches("123456", "otp-hash")).thenReturn(true);
        when(userRepository.findByEmail("user@utetra.vn")).thenReturn(Optional.of(user));

        service.reset("user@utetra.vn", "123456", "Newpass123");

        assertEquals("encoded", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(otpRepository).delete(pending);
    }

    @Test
    void invalidOtpCountsAttempt() {
        PasswordResetOtp pending = pending();
        when(otpRepository.findLockedByEmail("user@utetra.vn")).thenReturn(Optional.of(pending));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidOtpCodeException.class,
                () -> service.reset("user@utetra.vn", "000000", "Newpass123"));

        assertEquals(1, pending.getAttempts());
        verify(otpRepository).save(pending);
        verify(userRepository, never()).save(any());
    }

    private static User customer() {
        User user = new User();
        user.setEmail("user@utetra.vn");
        user.setRole(Role.USER);
        user.setActive(true);
        return user;
    }

    private static PasswordResetOtp pending() {
        PasswordResetOtp pending = new PasswordResetOtp();
        pending.setEmail("user@utetra.vn");
        pending.setOtpHash("otp-hash");
        pending.setAttempts(0);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        pending.setResendAvailableAt(LocalDateTime.now().minusSeconds(1));
        return pending;
    }
}
