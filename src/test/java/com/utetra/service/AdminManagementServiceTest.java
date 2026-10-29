package com.utetra.service;

import com.utetra.dto.AdminBranchForm;
import com.utetra.dto.AdminUserForm;
import com.utetra.entity.Branch;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminManagementServiceTest {

    @Test
    void cannotDeactivateBranchWithActiveStaff() {
        BranchRepository branches = mock(BranchRepository.class);
        UserRepository users = mock(UserRepository.class);
        AdminBranchService service = new AdminBranchService(branches, users);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setActive(true);
        when(users.existsByRoleAndBranchIdAndActiveTrue(Role.STAFF, 1L)).thenReturn(true);

        assertThrows(AdminManagementException.class, () -> service.toggleActive(branch));
        verify(branches, never()).save(any());
    }

    @Test
    void createBranchTrimsValues() {
        BranchRepository branches = mock(BranchRepository.class);
        AdminBranchService service = new AdminBranchService(branches, mock(UserRepository.class));
        AdminBranchForm form = new AdminBranchForm();
        form.setName("  UTETra Mới ");
        form.setAddress("  12 Lê Văn Việt ");
        form.setOpenTime(LocalTime.of(8, 0));
        form.setCloseTime(LocalTime.of(22, 0));
        form.setActive(true);
        when(branches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Branch saved = service.create(form);

        assertEquals("UTETra Mới", saved.getName());
        assertEquals("12 Lê Văn Việt", saved.getAddress());
    }

    @Test
    void createStaffRequiresAndAssignsActiveBranch() {
        UserRepository users = mock(UserRepository.class);
        BranchRepository branches = mock(BranchRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AdminUserService service = new AdminUserService(users, branches, encoder);
        Branch branch = new Branch();
        branch.setId(2L);
        branch.setActive(true);
        when(branches.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(branch));
        when(encoder.encode("Password1")).thenReturn("hash");
        when(users.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AdminUserForm form = staffForm();

        User saved = service.create(form);

        assertEquals(Role.STAFF, saved.getRole());
        assertSame(branch, saved.getBranch());
        assertEquals("staff@utetra.vn", saved.getEmail());
        assertEquals("hash", saved.getPasswordHash());
    }

    @Test
    void adminAccountCannotBeDisabled() {
        AdminUserService service = new AdminUserService(mock(UserRepository.class),
                mock(BranchRepository.class), mock(PasswordEncoder.class));
        User admin = new User();
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        assertThrows(AdminManagementException.class, () -> service.toggleActive(admin));
    }

    private static AdminUserForm staffForm() {
        AdminUserForm form = new AdminUserForm();
        form.setFullName("Nhân viên mới");
        form.setEmail(" STAFF@utetra.vn ");
        form.setPhone("0900000000");
        form.setRole(Role.STAFF);
        form.setBranchId(2L);
        form.setPassword("Password1");
        form.setActive(true);
        return form;
    }
}
