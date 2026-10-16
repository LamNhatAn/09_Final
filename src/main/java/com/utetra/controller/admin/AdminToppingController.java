package com.utetra.controller.admin;

import com.utetra.dto.ToppingForm;
import com.utetra.entity.Topping;
import com.utetra.service.AdminToppingService;
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

/** Quan ly topping (chi ADMIN, da chan boi SecurityConfig: /admin/** hasRole ADMIN). */
@Controller
@RequestMapping("/admin/toppings")
@RequiredArgsConstructor
public class AdminToppingController {

    private static final String LIST_REDIRECT = "redirect:/admin/toppings";

    private final AdminToppingService toppingService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("toppings", toppingService.findAll());
        return "admin/toppings/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("toppingForm", new ToppingForm());
        model.addAttribute("topping", null);
        return "admin/toppings/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("toppingForm") ToppingForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        checkDuplicate(form, null, result);
        if (result.hasErrors()) {
            model.addAttribute("topping", null);
            return "admin/toppings/form";
        }
        try {
            toppingService.create(form);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("name", "name.duplicate", "Tên topping này đã tồn tại");
            model.addAttribute("topping", null);
            return "admin/toppings/form";
        }
        redirect.addAttribute("ok", "created");
        return LIST_REDIRECT;
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Topping topping = findOr404(id);
        ToppingForm form = new ToppingForm();
        form.setName(topping.getName());
        form.setPrice(topping.getPrice());
        form.setAvailable(topping.isAvailable());
        model.addAttribute("toppingForm", form);
        model.addAttribute("topping", topping);
        return "admin/toppings/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("toppingForm") ToppingForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Topping topping = findOr404(id);
        checkDuplicate(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("topping", topping);
            return "admin/toppings/form";
        }
        try {
            toppingService.update(topping, form);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("name", "name.duplicate", "Tên topping này đã tồn tại");
            model.addAttribute("topping", topping);
            return "admin/toppings/form";
        }
        redirect.addAttribute("ok", "updated");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id:\\d+}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        boolean available = toppingService.toggleAvailable(findOr404(id));
        redirect.addAttribute("ok", available ? "shown" : "hidden");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id:\\d+}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            toppingService.delete(findOr404(id));
            redirect.addAttribute("ok", "deleted");
        } catch (EntityInUseException | DataIntegrityViolationException e) {
            redirect.addAttribute("err", "in_use");
        }
        return LIST_REDIRECT;
    }

    private void checkDuplicate(ToppingForm form, Long excludeId, BindingResult result) {
        if (!result.hasFieldErrors("name") && toppingService.nameTaken(form.getName(), excludeId)) {
            result.rejectValue("name", "name.duplicate", "Tên topping này đã tồn tại");
        }
    }

    private Topping findOr404(Long id) {
        return toppingService.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay topping"));
    }
}
