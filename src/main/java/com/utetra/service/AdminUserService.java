package com.utetra.service;

import com.utetra.dto.AdminUserForm;
import com.utetra.entity.Branch;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int PAGE_SIZE = 12;

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<User> list(Role role, Boolean active, String keyword, int page) {
        PageRequest pageable = PageRequest.of(Math.max(1, page) - 1, PAGE_SIZE,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<User> result = userRepository.searchManagement(role, active, searchPattern(keyword), pageable);
        if (result.isEmpty() && result.getTotalPages() > 0 && pageable.getPageNumber() >= result.getTotalPages()) {
            result = userRepository.searchManagement(role, active, searchPattern(keyword),
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, pageable.getSort()));
        }
        return result;
    }

    public Optional<User> find(Long id) {
        return userRepository.findById(id);
    }

    public boolean emailTaken(String email, Long excludeId) {
        String value = normalizeEmail(email);
        return excludeId == null
                ? userRepository.existsByEmailIgnoreCase(value)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(value, excludeId);
    }

    @Transactional
    public User create(AdminUserForm form) {
        validateRoleAndBranch(form);
        User user = new User();
        apply(user, form);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        return userRepository.saveAndFlush(user);
    }

    @Transactional
    public User update(User user, AdminUserForm form) {
        ensureEditable(user);
        validateRoleAndBranch(form);
        apply(user, form);
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        return userRepository.saveAndFlush(user);
    }

    @Transactional
    public boolean toggleActive(User user) {
        ensureEditable(user);
        if (!user.isActive() && user.getRole() == Role.STAFF
                && (user.getBranch() == null || !user.getBranch().isActive())) {
            throw new AdminManagementException("Không thể kích hoạt nhân viên tại chi nhánh đang ẩn");
        }
        user.setActive(!user.isActive());
        return userRepository.save(user).isActive();
    }

    private void validateRoleAndBranch(AdminUserForm form) {
        if (form.getRole() == null || form.getRole() == Role.ADMIN) {
            throw new AdminManagementException("Chỉ được quản lý tài khoản khách hàng hoặc nhân viên");
        }
        if (form.getRole() == Role.STAFF && form.getBranchId() == null) {
            throw new AdminManagementException("Nhân viên phải thuộc một chi nhánh");
        }
    }

    private void apply(User user, AdminUserForm form) {
        user.setFullName(form.getFullName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setPhone(clean(form.getPhone()));
        user.setAddress(clean(form.getAddress()));
        user.setRole(form.getRole());
        user.setActive(form.isActive());
        if (form.getRole() == Role.STAFF) {
            Branch branch = branchRepository.findByIdAndActiveTrue(form.getBranchId())
                    .orElseThrow(() -> new AdminManagementException("Chi nhánh không tồn tại hoặc đang ẩn"));
            user.setBranch(branch);
        } else {
            user.setBranch(null);
        }
    }

    private static void ensureEditable(User user) {
        if (user.getRole() == Role.ADMIN) {
            throw new AdminManagementException("Không thể sửa hoặc khóa tài khoản quản trị tại đây");
        }
    }

    private static String searchPattern(String keyword) {
        String value = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 100) {
            value = value.substring(0, 100);
        }
        return "%" + value.replace("!", "!!").replace("%", "!%")
                .replace("_", "!_").replace("[", "![") + "%";
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
