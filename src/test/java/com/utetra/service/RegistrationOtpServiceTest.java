package com.utetra.service;

import com.utetra.dto.RegisterForm;
import com.utetra.entity.RegistrationOtp;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.RegistrationOtpRepository;
import com.utetra.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationOtpServiceTest {

    private RegistrationOtpRepository otpRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RegistrationOtpMailSender mailSender;
    private RegistrationOtpService service;

    @BeforeEach
    void setUp() {
        otpRepository = mock(RegistrationOtpRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        mailSender = mock(RegistrationOtpMailSender.class);
        service = new RegistrationOtpService(otpRepository, userRepository, passwordEncoder, mailSender,
                10, 60, 5);
    }

    @Test
    void startStoresHashedPendingRegistrationAndEmailsSixDigitCode() {
        RegisterForm form = form();
        when(userRepository.existsByEmail("new@utetra.vn")).thenReturn(false);
        when(otpRepository.findLockedByEmail("new@utetra.vn")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-hash");

        assertEquals("new@utetra.vn", service.start(form));

        ArgumentCaptor<RegistrationOtp> pending = ArgumentCaptor.forClass(RegistrationOtp.class);
        verify(otpRepository).saveAndFlush(pending.capture());
        assertEquals("New User", pending.getValue().getFullName());
        assertEquals("bcrypt-hash", pending.getValue().getPasswordHash());
        assertEquals("bcrypt-hash", pending.getValue().getOtpHash());
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mailSender).send(org.mockito.ArgumentMatchers.eq("new@utetra.vn"), code.capture(),
                org.mockito.ArgumentMatchers.eq(10L));
        assertTrue(code.getValue().matches("[0-9]{6}"));
    }

    @Test
    void verifyCreatesUserOnlyAfterCorrectOtp() {
        RegistrationOtp pending = pending();
        when(otpRepository.findLockedByEmail("new@utetra.vn")).thenReturn(Optional.of(pending));
        when(passwordEncoder.matches("123456", "otp-hash")).thenReturn(true);
        when(userRepository.existsByEmail("new@utetra.vn")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.verify("NEW@UTETRA.VN", "123456");

        assertEquals("new@utetra.vn", user.getEmail());
        assertEquals("password-hash", user.getPasswordHash());
        assertEquals(Role.USER, user.getRole());
        verify(otpRepository).delete(pending);
    }

    @Test
    void verifyWrongOtpIncrementsAttemptsAndDoesNotCreateUser() {
        RegistrationOtp pending = pending();
        when(otpRepository.findLockedByEmail("new@utetra.vn")).thenReturn(Optional.of(pending));
        when(passwordEncoder.matches("000000", "otp-hash")).thenReturn(false);

        assertThrows(InvalidOtpCodeException.class,
                () -> service.verify("new@utetra.vn", "000000"));

        assertEquals(1, pending.getAttempts());
        verify(otpRepository).save(pending);
        verify(userRepository, never()).saveAndFlush(any());
    }

    private static RegisterForm form() {
        RegisterForm form = new RegisterForm();
        form.setFullName(" New User ");
        form.setEmail(" NEW@UTETRA.VN ");
        form.setPhone("0901234567");
        form.setPassword("Password123");
        form.setConfirmPassword("Password123");
        return form;
    }

    private static RegistrationOtp pending() {
        RegistrationOtp pending = new RegistrationOtp();
        pending.setEmail("new@utetra.vn");
        pending.setFullName("New User");
        pending.setPhone("0901234567");
        pending.setPasswordHash("password-hash");
        pending.setOtpHash("otp-hash");
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        pending.setResendAvailableAt(LocalDateTime.now().minusSeconds(1));
        return pending;
    }
}
