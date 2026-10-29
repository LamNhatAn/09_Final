package com.utetra.controller.web;

import com.utetra.dto.LoginForm;
import com.utetra.dto.RegisterForm;
import com.utetra.dto.RegistrationOtpForm;
import com.utetra.entity.enums.Role;
import com.utetra.security.AppUserDetails;
import com.utetra.security.AuthCookieService;
import com.utetra.security.JwtService;
import com.utetra.service.EmailAlreadyUsedException;
import com.utetra.service.EmailDeliveryException;
import com.utetra.service.RegistrationOtpException;
import com.utetra.service.RegistrationOtpService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthCookieService cookieService;
    private final RegistrationOtpService registrationOtpService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          AuthCookieService cookieService, RegistrationOtpService registrationOtpService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.registrationOtpService = registrationOtpService;
    }

    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal AppUserDetails user,
                            @RequestParam(required = false) String next, Model model) {
        if (user != null) {
            return "redirect:" + homeFor(user.getRole());
        }
        LoginForm form = new LoginForm();
        form.setNext(next);
        model.addAttribute("loginForm", form);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form, BindingResult result,
                        HttpServletResponse response) {
        if (result.hasErrors()) {
            return "auth/login";
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(form.getEmail().trim(), form.getPassword()));
            AppUserDetails user = (AppUserDetails) auth.getPrincipal();
            response.addHeader(HttpHeaders.SET_COOKIE, cookieService.create(jwtService.generate(user)));
            String next = safeNext(form.getNext());
            return "redirect:" + (next != null ? next : homeFor(user.getRole()));
        } catch (DisabledException e) {
            result.reject("login.disabled", "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        } catch (AuthenticationException e) {
            // Cung mot thong bao cho "sai email" va "sai mat khau" de khong lo email nao ton tai
            result.reject("login.bad", "Email hoặc mật khẩu không đúng.");
        }
        form.setPassword(null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal AppUserDetails user, Model model) {
        if (user != null) {
            return "redirect:" + homeFor(user.getRole());
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form, BindingResult result) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!result.hasErrors()) {
            try {
                String email = registrationOtpService.start(form);
                return "redirect:/register/verify?email=" + urlEncode(email);
            } catch (EmailAlreadyUsedException e) {
                result.rejectValue("email", "duplicate", "Email này đã được đăng ký");
            } catch (EmailDeliveryException e) {
                result.reject("email.send", "Không gửi được OTP. Vui lòng kiểm tra cấu hình email gửi và thử lại.");
            } catch (RegistrationOtpException e) {
                result.reject("otp.start", e.getMessage());
            }
        }
        form.setPassword(null);
        form.setConfirmPassword(null);
        return "auth/register";
    }

    @GetMapping("/register/verify")
    public String verifyRegistrationPage(@AuthenticationPrincipal AppUserDetails user,
                                         @RequestParam(required = false) String email,
                                         @RequestParam(required = false) String sent,
                                         Model model) {
        if (user != null) {
            return "redirect:" + homeFor(user.getRole());
        }
        if (email == null || email.isBlank()) {
            return "redirect:/register";
        }
        RegistrationOtpForm form = new RegistrationOtpForm();
        form.setEmail(email.trim());
        model.addAttribute("otpForm", form);
        model.addAttribute("maskedEmail", maskEmail(email));
        model.addAttribute("sentMessage", "1".equals(sent) ? "Đã gửi một mã OTP mới." : null);
        return "auth/register-verify";
    }

    @PostMapping("/register/verify")
    public String verifyRegistration(@Valid @ModelAttribute("otpForm") RegistrationOtpForm form,
                                     BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                registrationOtpService.verify(form.getEmail(), form.getCode());
                return "redirect:/login?registered";
            } catch (RegistrationOtpException e) {
                result.rejectValue("code", "otp.invalid", e.getMessage());
            }
        }
        form.setCode(null);
        model.addAttribute("maskedEmail", maskEmail(form.getEmail()));
        return "auth/register-verify";
    }

    @PostMapping("/register/resend")
    public String resendRegistrationOtp(@RequestParam String email, Model model) {
        RegistrationOtpForm form = new RegistrationOtpForm();
        form.setEmail(email == null ? "" : email.trim());
        try {
            registrationOtpService.resend(email);
            return "redirect:/register/verify?email=" + urlEncode(form.getEmail()) + "&sent=1";
        } catch (RegistrationOtpException | EmailDeliveryException e) {
            model.addAttribute("otpForm", form);
            model.addAttribute("maskedEmail", maskEmail(email));
            model.addAttribute("resendError", e.getMessage());
            return "auth/register-verify";
        }
    }

    private static String homeFor(Role role) {
        return switch (role) {
            case ADMIN -> "/admin/dashboard";
            case STAFF -> "/staff/dashboard";
            default -> "/";
        };
    }

    /** Chi cho phep quay lai duong dan noi bo, chan open redirect (//evil.com, http://...). */
    private static String safeNext(String next) {
        if (next == null || next.isBlank()) {
            return null;
        }
        boolean internal = next.startsWith("/") && !next.startsWith("//") && !next.startsWith("/\\")
                && next.chars().noneMatch(c -> c < 0x20 || c == '\\');
        return internal && !next.startsWith("/login") && !next.startsWith("/register") ? next : null;
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "email của bạn";
        }
        String value = email.trim();
        int at = value.indexOf('@');
        String local = value.substring(0, at);
        String visible = local.length() <= 2 ? local.substring(0, 1) : local.substring(0, 2);
        return visible + "***" + value.substring(at);
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
