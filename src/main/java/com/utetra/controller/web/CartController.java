package com.utetra.controller.web;

import com.utetra.dto.AddCartItemForm;
import com.utetra.security.AppUserDetails;
import com.utetra.service.CartException;
import com.utetra.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String view(@AuthenticationPrincipal AppUserDetails user,
                       @RequestParam(required = false) String ok,
                       @RequestParam(required = false) String err,
                       Model model) {
        model.addAttribute("cart", cartService.getView(user.getId()));
        model.addAttribute("okMessage", "added".equals(ok) ? "Đã thêm món vào giỏ hàng." : null);
        model.addAttribute("errMessage", switch (err == null ? "" : err) {
            case "invalid" -> "Không thể cập nhật giỏ hàng. Vui lòng kiểm tra lại lựa chọn.";
            case "changed" -> "Một số món đã thay đổi hoặc ngừng phục vụ.";
            default -> null;
        });
        return "cart";
    }

    @PostMapping("/items")
    public String add(@AuthenticationPrincipal AppUserDetails user,
                      @Valid AddCartItemForm form, BindingResult result,
                      RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return productError(form.getProductId(), redirect);
        }
        try {
            cartService.add(user.getId(), form);
            redirect.addAttribute("ok", "added");
            return "redirect:/cart";
        } catch (CartException e) {
            return productError(form.getProductId(), redirect);
        }
    }

    @PostMapping("/items/{id:\\d+}/quantity")
    public String updateQuantity(@AuthenticationPrincipal AppUserDetails user,
                                 @PathVariable Long id, @RequestParam String quantity,
                                 RedirectAttributes redirect) {
        try {
            cartService.updateQuantity(user.getId(), id, Integer.parseInt(quantity));
        } catch (CartException | NumberFormatException e) {
            redirect.addAttribute("err", "invalid");
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{id:\\d+}/delete")
    public String remove(@AuthenticationPrincipal AppUserDetails user,
                         @PathVariable Long id, RedirectAttributes redirect) {
        try {
            cartService.remove(user.getId(), id);
        } catch (CartException e) {
            redirect.addAttribute("err", "invalid");
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(@AuthenticationPrincipal AppUserDetails user) {
        cartService.clear(user.getId());
        return "redirect:/cart";
    }

    private static String productError(Long productId, RedirectAttributes redirect) {
        redirect.addAttribute("cartError", "invalid");
        return productId == null ? "redirect:/menu" : "redirect:/products/" + productId;
    }
}
