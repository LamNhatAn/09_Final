package com.utetra.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** JWT duoc giu trong cookie HttpOnly nen JavaScript (XSS) khong doc duoc token. */
@Component
public class AuthCookieService {

    public static final String COOKIE_NAME = "UTETRA_TOKEN";

    private final JwtService jwtService;
    private final boolean secure;

    public AuthCookieService(JwtService jwtService, @Value("${app.cookie.secure}") boolean secure) {
        this.jwtService = jwtService;
        this.secure = secure;
    }

    public String create(String token) {
        return base(token).maxAge(jwtService.getTtl()).build().toString();
    }

    public String clear() {
        return base("").maxAge(0).build().toString();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/");
    }

    public static String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
