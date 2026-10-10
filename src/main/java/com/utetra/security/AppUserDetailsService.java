package com.utetra.security;

import com.utetra.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Nap user tu SQL Server theo email. Dung cho ca luc dang nhap (AuthenticationManager)
 * lan moi request co JWT (JwtAuthenticationFilter).
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AppUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay nguoi dung"));
    }
}
