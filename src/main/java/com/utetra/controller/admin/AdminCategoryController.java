package com.utetra.controller.admin;

import com.utetra.dto.CategoryForm;
import com.utetra.entity.Category;
import com.utetra.service.AdminCategoryService;
import com.utetra.service.EntityInUseException;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quan ly danh muc (chi ADMIN, da chan boi SecurityConfig: /admin/** hasRole ADMIN). */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private static final String LIST_REDIRECT = "redirect:/admin/categories";

    private final AdminCategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("productCounts", categoryService.productCounts());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("category", null);
        return "admin/categories/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        checkDuplicate(form, null, result);
        if (result.hasErrors()) {
            model.addAttribute("category", null);
            return "admin/categories/form";
        }
        try {
            categoryService.create(form);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("name", "name.duplicate", "Tên danh mục này đã tồn tại");
            model.addAttribute("category", null);
            return "admin/categories/form";
        }
        redirect.addAttribute("ok", "created");
        return LIST_REDIRECT;
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = findOr404(id);
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        form.setActive(category.isActive());
        model.addAttribute("categoryForm", form);
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Category category = findOr404(id);
        checkDuplicate(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            return "admin/categories/form";
        }
        try {
            categoryService.update(category, form);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("name", "name.duplicate", "Tên danh mục này đã tồn tại");
            model.addAttribute("category", category);
            return "admin/categories/form";
        }
        redirect.addAttribute("ok", "updated");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id:\\d+}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        boolean active = categoryService.toggleActive(findOr404(id));
        redirect.addAttribute("ok", active ? "shown" : "hidden");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id:\\d+}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categoryService.delete(findOr404(id));
            redirect.addAttribute("ok", "deleted");
        } catch (EntityInUseException | DataIntegrityViolationException e) {
            redirect.addAttribute("err", "in_use");
        }
        return LIST_REDIRECT;
    }

    private void checkDuplicate(CategoryForm form, Long excludeId, BindingResult result) {
        if (!result.hasFieldErrors("name") && categoryService.nameTaken(form.getName(), excludeId)) {
            result.rejectValue("name", "name.duplicate", "Tên danh mục này đã tồn tại");
        }
    }

    private Category findOr404(Long id) {
        return categoryService.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc"));
    }
}
