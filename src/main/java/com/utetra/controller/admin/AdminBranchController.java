package com.utetra.controller.admin;

import com.utetra.dto.AdminBranchForm;
import com.utetra.entity.Branch;
import com.utetra.service.AdminBranchService;
import com.utetra.service.AdminManagementException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/branches")
@RequiredArgsConstructor
public class AdminBranchController {

    private static final String REDIRECT = "redirect:/admin/branches";
    private final AdminBranchService branchService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", branchService.findAll());
        return "admin/branches/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("branchForm", new AdminBranchForm());
        model.addAttribute("branch", null);
        return "admin/branches/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("branchForm") AdminBranchForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        validate(form, null, result);
        if (result.hasErrors()) {
            model.addAttribute("branch", null);
            return "admin/branches/form";
        }
        branchService.create(form);
        redirect.addAttribute("ok", "created");
        return REDIRECT;
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Branch branch = findOr404(id);
        AdminBranchForm form = new AdminBranchForm();
        form.setName(branch.getName());
        form.setAddress(branch.getAddress());
        form.setPhone(branch.getPhone());
        form.setOpenTime(branch.getOpenTime());
        form.setCloseTime(branch.getCloseTime());
        form.setActive(branch.isActive());
        model.addAttribute("branchForm", form);
        model.addAttribute("branch", branch);
        return "admin/branches/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("branchForm") AdminBranchForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Branch branch = findOr404(id);
        validate(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("branch", branch);
            return "admin/branches/form";
        }
        try {
            branchService.update(branch, form);
        } catch (AdminManagementException e) {
            result.rejectValue("active", "branch.inUse", e.getMessage());
            model.addAttribute("branch", branch);
            return "admin/branches/form";
        }
        redirect.addAttribute("ok", "updated");
        return REDIRECT;
    }

    @PostMapping("/{id:\\d+}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            boolean active = branchService.toggleActive(findOr404(id));
            redirect.addAttribute("ok", active ? "activated" : "deactivated");
        } catch (AdminManagementException e) {
            redirect.addAttribute("err", "branch_has_staff");
        }
        return REDIRECT;
    }

    private void validate(AdminBranchForm form, Long excludeId, BindingResult result) {
        if (!result.hasFieldErrors("name") && branchService.nameTaken(form.getName(), excludeId)) {
            result.rejectValue("name", "name.duplicate", "Tên chi nhánh này đã tồn tại");
        }
        if (form.getOpenTime() != null && form.getCloseTime() != null
                && !form.getOpenTime().isBefore(form.getCloseTime())) {
            result.rejectValue("closeTime", "time.invalid", "Giờ đóng cửa phải sau giờ mở cửa");
        }
    }

    private Branch findOr404(Long id) {
        return branchService.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chi nhánh"));
    }
}
