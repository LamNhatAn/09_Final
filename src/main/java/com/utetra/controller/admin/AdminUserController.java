package com.utetra.controller.admin;

import com.utetra.dto.AdminUserForm;
import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import com.utetra.repository.BranchRepository;
import com.utetra.service.AdminManagementException;
import com.utetra.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private static final String REDIRECT = "redirect:/admin/users";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AdminUserService userService;
    private final BranchRepository branchRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Role role,
                       @RequestParam(required = false) Boolean active,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        model.addAttribute("users", userService.list(role, active, q, page));
        model.addAttribute("roles", Role.values());
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedActive", active);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new AdminUserForm());
        prepareForm(model, null);
        return "admin/users/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("userForm") AdminUserForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        validate(form, null, true, result);
        if (result.hasErrors()) {
            prepareForm(model, null);
            return "admin/users/form";
        }
        try {
            userService.create(form);
        } catch (AdminManagementException e) {
            result.reject("user.invalid", e.getMessage());
            prepareForm(model, null);
            return "admin/users/form";
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "email.duplicate", "Email này đã được sử dụng");
            prepareForm(model, null);
            return "admin/users/form";
        }
        redirect.addAttribute("ok", "created");
        return REDIRECT;
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        User user = findOr404(id);
        if (user.getRole() == Role.ADMIN) {
            redirect.addAttribute("err", "admin_protected");
            return REDIRECT;
        }
        AdminUserForm form = toForm(user);
        model.addAttribute("userForm", form);
        prepareForm(model, user);
        return "admin/users/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("userForm") AdminUserForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        User user = findOr404(id);
        validate(form, id, false, result);
        if (result.hasErrors()) {
            prepareForm(model, user);
            return "admin/users/form";
        }
        try {
            userService.update(user, form);
        } catch (AdminManagementException e) {
            result.reject("user.invalid", e.getMessage());
            prepareForm(model, user);
            return "admin/users/form";
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "email.duplicate", "Email này đã được sử dụng");
            prepareForm(model, user);
            return "admin/users/form";
        }
        redirect.addAttribute("ok", "updated");
        return REDIRECT;
    }

    @PostMapping("/{id:\\d+}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        User user = findOr404(id);
        try {
            boolean active = userService.toggleActive(user);
            redirect.addAttribute("ok", active ? "activated" : "deactivated");
        } catch (AdminManagementException e) {
            redirect.addAttribute("err", user.getRole() == Role.ADMIN ? "admin_protected" : "user_activation");
        }
        return REDIRECT;
    }

    private void validate(AdminUserForm form, Long excludeId, boolean creating, BindingResult result) {
        if (!result.hasFieldErrors("email") && userService.emailTaken(form.getEmail(), excludeId)) {
            result.rejectValue("email", "email.duplicate", "Email này đã được sử dụng");
        }
        if (creating && (form.getPassword() == null || form.getPassword().length() < 8)) {
            result.rejectValue("password", "password.short", "Mật khẩu phải có ít nhất 8 ký tự");
        } else if (!creating && form.getPassword() != null && !form.getPassword().isBlank()
                && form.getPassword().length() < 8) {
            result.rejectValue("password", "password.short", "Mật khẩu mới phải có ít nhất 8 ký tự");
        }
        if (form.getRole() == Role.ADMIN) {
            result.rejectValue("role", "role.invalid", "Không thể cấp quyền quản trị từ màn hình này");
        }
        if (form.getRole() == Role.STAFF && form.getBranchId() == null) {
            result.rejectValue("branchId", "branch.required", "Vui lòng chọn chi nhánh cho nhân viên");
        }
    }

    private void prepareForm(Model model, User user) {
        model.addAttribute("managedUser", user);
        model.addAttribute("branches", branchRepository.findByActiveTrueOrderByIdAsc());
    }

    private User findOr404(Long id) {
        return userService.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
    }

    private static AdminUserForm toForm(User user) {
        AdminUserForm form = new AdminUserForm();
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setAddress(user.getAddress());
        form.setRole(user.getRole());
        form.setBranchId(user.getBranch() == null ? null : user.getBranch().getId());
        form.setActive(user.isActive());
        return form;
    }
}
