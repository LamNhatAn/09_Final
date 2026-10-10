package com.utetra.security;

import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    private static AppUserDetails sampleUser() {
        User user = new User();
        user.setEmail("user1@utetra.vn");
        user.setFullName("User Mot");
        user.setPasswordHash("x");
        user.setRole(Role.USER);
        return new AppUserDetails(user);
    }

    @Test
    void generatedTokenCanBeParsed() {
        JwtService service = new JwtService(SECRET, 60);
        Claims claims = service.parse(service.generate(sampleUser()));
        assertEquals("user1@utetra.vn", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(SECRET, 60);
        String token = service.generate(sampleUser());
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThrows(JwtException.class, () -> service.parse(tampered));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = new JwtService("another-secret-another-secret-123456", 60).generate(sampleUser());
        assertThrows(JwtException.class, () -> new JwtService(SECRET, 60).parse(token));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = new JwtService(SECRET, -1);
        assertThrows(JwtException.class, () -> service.parse(service.generate(sampleUser())));
    }

    @Test
    void shortSecretIsRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 60));
    }
}
