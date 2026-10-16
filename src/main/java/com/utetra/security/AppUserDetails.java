package com.utetra.security;

import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Thong tin nguoi dung dang dang nhap. JSP doc qua bien ${currentUser}, controller qua @AuthenticationPrincipal.
 */
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final Long branchId;
    private final boolean active;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.branchId = user.getBranch() == null ? null : user.getBranch().getId();
        this.active = user.isActive();
    }

    public Long getId() { return id; }

    public String getEmail() { return email; }

    public String getFullName() { return fullName; }

    public Role getRole() { return role; }

    public Long getBranchId() { return branchId; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    public boolean isStaff() { return role == Role.STAFF; }

    public boolean isCustomer() { return role == Role.USER; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isEnabled() { return active; }
}
