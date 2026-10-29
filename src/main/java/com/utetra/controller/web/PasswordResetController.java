package com.utetra.controller.web;

import com.utetra.dto.ForgotPasswordForm;
import com.utetra.dto.ResetPasswordForm;
import com.utetra.service.EmailDeliveryException;
import com.utetra.service.PasswordResetException;
import com.utetra.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String requestPage(Model model) {
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String request(@Valid @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
                          BindingResult result) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }
        try {
            String email = passwordResetService.start(form.getEmail());
            return "redirect:/forgot-password/verify?email=" + encode(email);
        } catch (EmailDeliveryException e) {
            result.reject("mail.failed", "Không gửi được email OTP. Vui lòng thử lại sau.");
        } catch (PasswordResetException e) {
            result.reject("reset.failed", e.getMessage());
        }
        return "auth/forgot-password";
    }

    @GetMapping("/forgot-password/verify")
    public String verifyPage(@RequestParam(required = false) String email,
                             @RequestParam(required = false) String sent, Model model) {
        if (email == null || email.isBlank()) {
            return "redirect:/forgot-password";
        }
        ResetPasswordForm form = new ResetPasswordForm();
        form.setEmail(email.trim());
        model.addAttribute("resetPasswordForm", form);
        prepare(model, email, "1".equals(sent) ? "Đã gửi một mã OTP mới." : null, null);
        return "auth/forgot-password-verify";
    }

    @PostMapping("/forgot-password/verify")
    public String verify(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                         BindingResult result, Model model) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!result.hasErrors()) {
            try {
                passwordResetService.reset(form.getEmail(), form.getCode(), form.getPassword());
                return "redirect:/login?password-reset";
            } catch (PasswordResetException e) {
                result.rejectValue("code", "otp.invalid", e.getMessage());
            }
        }
        form.setCode(null);
        form.setPassword(null);
        form.setConfirmPassword(null);
        prepare(model, form.getEmail(), null, null);
        return "auth/forgot-password-verify";
    }

    @PostMapping("/forgot-password/resend")
    public String resend(@RequestParam String email, Model model) {
        try {
            passwordResetService.resend(email);
            return "redirect:/forgot-password/verify?email=" + encode(email) + "&sent=1";
        } catch (PasswordResetException | EmailDeliveryException e) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setEmail(email);
            model.addAttribute("resetPasswordForm", form);
            prepare(model, email, null, e.getMessage());
            return "auth/forgot-password-verify";
        }
    }

    private static void prepare(Model model, String email, String sent, String error) {
        model.addAttribute("maskedEmail", mask(email));
        model.addAttribute("sentMessage", sent);
        model.addAttribute("resendError", error);
    }

    private static String mask(String email) {
        if (email == null || !email.contains("@")) return "email của bạn";
        String value = email.trim();
        int at = value.indexOf('@');
        String local = value.substring(0, at);
        return local.substring(0, Math.min(2, local.length())) + "***" + value.substring(at);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
