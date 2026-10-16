package com.utetra.controller.web;

import com.utetra.dto.CheckoutForm;
import com.utetra.entity.Order;
import com.utetra.security.AppUserDetails;
import com.utetra.service.CartService;
import com.utetra.service.CatalogService;
import com.utetra.service.CheckoutException;
import com.utetra.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CartService cartService;
    private final CatalogService catalogService;

    @GetMapping
    public String form(@AuthenticationPrincipal AppUserDetails user, Model model) {
        if (cartService.getView(user.getId()).isEmpty()) {
            return "redirect:/cart?err=invalid";
        }
        model.addAttribute("checkoutForm", checkoutService.defaultForm(user.getId()));
        fillModel(user.getId(), model);
        return "checkout";
    }

    @PostMapping
    public String place(@AuthenticationPrincipal AppUserDetails user,
                        @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                        BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                Order order = checkoutService.placeCod(user.getId(), form);
                return "redirect:/checkout/success/" + order.getCode();
            } catch (CheckoutException e) {
                result.reject("checkout.invalid", e.getMessage());
            }
        }
        fillModel(user.getId(), model);
        return "checkout";
    }

    @GetMapping("/success/{code:[A-Za-z0-9-]+}")
    public String success(@AuthenticationPrincipal AppUserDetails user,
                          @PathVariable String code, Model model) {
        try {
            model.addAttribute("order", checkoutService.findSuccess(code, user.getId()));
            return "checkout-success";
        } catch (CheckoutException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
    }

    private void fillModel(Long userId, Model model) {
        model.addAttribute("cart", cartService.getView(userId));
        model.addAttribute("branches", catalogService.activeBranches());
    }
}
