package com.utetra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Moi request: doc JWT tu cookie -> kiem tra chu ky/han -> nap lai user tu DB
 * (de tai khoan bi khoa hoac doi role co hieu luc ngay) -> dat vao SecurityContext.
 * Khong khai bao la @Component de tranh Spring Boot dang ky filter nay hai lan.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String CURRENT_USER_ATTR = "currentUser";

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;
    private final AuthCookieService cookieService;

    public JwtAuthenticationFilter(JwtService jwtService, AppUserDetailsService userDetailsService,
                                   AuthCookieService cookieService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = AuthCookieService.read(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parse(token);
                AppUserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
                if (user.isEnabled()) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    request.setAttribute(CURRENT_USER_ATTR, user);
                } else {
                    response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clear());
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // Token het han / bi sua / user khong con: coi nhu chua dang nhap va xoa cookie hong
                response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clear());
            }
        }
        chain.doFilter(request, response);
    }
}
