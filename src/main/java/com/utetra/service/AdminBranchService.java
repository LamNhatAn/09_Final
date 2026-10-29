package com.utetra.service;

import com.utetra.dto.AdminBranchForm;
import com.utetra.entity.Branch;
import com.utetra.entity.enums.Role;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminBranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public List<Branch> findAll() {
        return branchRepository.findAllByOrderByIdAsc();
    }

    public Optional<Branch> find(Long id) {
        return branchRepository.findById(id);
    }

    public boolean nameTaken(String name, Long excludeId) {
        String value = name == null ? "" : name.trim();
        return excludeId == null
                ? branchRepository.existsByNameIgnoreCase(value)
                : branchRepository.existsByNameIgnoreCaseAndIdNot(value, excludeId);
    }

    @Transactional
    public Branch create(AdminBranchForm form) {
        Branch branch = new Branch();
        apply(branch, form);
        return branchRepository.save(branch);
    }

    @Transactional
    public Branch update(Branch branch, AdminBranchForm form) {
        if (branch.isActive() && !form.isActive() && hasActiveStaff(branch.getId())) {
            throw new AdminManagementException("Chi nhánh còn nhân viên đang hoạt động");
        }
        apply(branch, form);
        return branchRepository.save(branch);
    }

    @Transactional
    public boolean toggleActive(Branch branch) {
        if (branch.isActive() && hasActiveStaff(branch.getId())) {
            throw new AdminManagementException("Chi nhánh còn nhân viên đang hoạt động");
        }
        branch.setActive(!branch.isActive());
        return branchRepository.save(branch).isActive();
    }

    private boolean hasActiveStaff(Long branchId) {
        return userRepository.existsByRoleAndBranchIdAndActiveTrue(Role.STAFF, branchId);
    }

    private static void apply(Branch branch, AdminBranchForm form) {
        branch.setName(form.getName().trim());
        branch.setAddress(form.getAddress().trim());
        branch.setPhone(clean(form.getPhone()));
        branch.setOpenTime(form.getOpenTime());
        branch.setCloseTime(form.getCloseTime());
        branch.setActive(form.isActive());
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
