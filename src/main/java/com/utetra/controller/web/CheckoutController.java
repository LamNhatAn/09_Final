package com.utetra.controller.web;

import com.utetra.dto.CheckoutForm;
import com.utetra.dto.CheckoutResult;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.security.AppUserDetails;
import com.utetra.service.CartService;
import com.utetra.service.CatalogService;
import com.utetra.service.CheckoutException;
import com.utetra.service.CheckoutService;
import com.utetra.service.VnpayGateway;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CartService cartService;
    private final CatalogService catalogService;
    private final VnpayGateway vnpayGateway;

    public CheckoutController(CheckoutService checkoutService, CartService cartService,
                              CatalogService catalogService, VnpayGateway vnpayGateway) {
        this.checkoutService = checkoutService;
        this.cartService = cartService;
        this.catalogService = catalogService;
        this.vnpayGateway = vnpayGateway;
    }

    @GetMapping
    public String form(@AuthenticationPrincipal AppUserDetails user, Model model) {
        var cart = cartService.getView(user.getId());
        if (cart.isEmpty()) {
            return "redirect:/cart?err=invalid";
        }
        if (cart.isSoldOutItemPresent()) {
            return "redirect:/cart?err=changed";
        }
        model.addAttribute("checkoutForm", checkoutService.defaultForm(user.getId()));
        fillModel(user.getId(), model);
        return "checkout";
    }

    @PostMapping
    public Object place(@AuthenticationPrincipal AppUserDetails user,
                        @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                        BindingResult result, Model model, HttpServletRequest request) {
        if (!result.hasErrors() && form.getPaymentMethod() == PaymentMethod.VNPAY
                && !vnpayGateway.isConfigured()) {
            result.rejectValue("paymentMethod", "vnpay.notConfigured",
                    "VNPay chưa được cấu hình trên máy này");
        }
        if (!result.hasErrors()) {
            try {
                CheckoutResult checkout = checkoutService.place(user.getId(), form);
                if (checkout.requiresVnpay()) {
                    String paymentUrl = vnpayGateway.createPaymentUrl(checkout.vnpayRequest(), request.getRemoteAddr());
                    RedirectView redirect = new RedirectView(paymentUrl);
                    redirect.setExposeModelAttributes(false);
                    return redirect;
                }
                return "redirect:/checkout/success/" + checkout.order().getCode();
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
        model.addAttribute("vnpayConfigured", vnpayGateway.isConfigured());
    }
}
