package com.utetra.config;

import com.utetra.security.AppUserDetailsService;
import com.utetra.security.AuthCookieService;
import com.utetra.security.JwtAuthenticationFilter;
import com.utetra.security.JwtService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Phan quyen: khach (chua dang nhap), USER, STAFF, ADMIN.
 * Xac thuc bang JWT trong cookie HttpOnly, khong dung HttpSession.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
                                           AppUserDetailsService userDetailsService,
                                           AuthCookieService cookieService) throws Exception {
        http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // CSRF token nam trong cookie XSRF-TOKEN (khong can session); form JSP gui kem _csrf
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(auth -> auth
                // Spring Security 6 kiem tra ca forward noi bo (controller -> file JSP), phai cho qua
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/login", "/register", "/403", "/error", "/health").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/cart/**", "/checkout/**", "/orders/**").hasRole("USER")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                // Chua dang nhap -> chuyen sang trang login (kem next de quay lai sau khi dang nhap)
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendRedirect(request.getContextPath() + "/login" + nextParam(request)))
                // Da dang nhap nhung sai quyen -> trang 403
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        response.sendRedirect(request.getContextPath() + "/403")))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .deleteCookies(AuthCookieService.COOKIE_NAME)
                .logoutSuccessUrl("/login?logout"))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService, cookieService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static String nextParam(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return "";
        }
        String target = request.getRequestURI().substring(request.getContextPath().length());
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }
        return "?next=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
