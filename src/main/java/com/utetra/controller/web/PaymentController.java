package com.utetra.controller.web;

import com.utetra.dto.VnpayCallbackOutcome;
import com.utetra.dto.VnpayPaymentRequest;
import com.utetra.security.AppUserDetails;
import com.utetra.service.VnpayException;
import com.utetra.service.VnpayGateway;
import com.utetra.service.VnpayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
@RequestMapping("/payments/vnpay")
public class PaymentController {

    private final VnpayPaymentService paymentService;
    private final VnpayGateway gateway;

    public PaymentController(VnpayPaymentService paymentService, VnpayGateway gateway) {
        this.paymentService = paymentService;
        this.gateway = gateway;
    }

    @GetMapping("/return")
    public String paymentReturn(@RequestParam Map<String, String> parameters, Model model) {
        VnpayCallbackOutcome outcome = paymentService.processCallback(parameters);
        model.addAttribute("outcome", outcome);
        return "payment-result";
    }

    @GetMapping("/ipn")
    @ResponseBody
    public Map<String, String> paymentIpn(@RequestParam Map<String, String> parameters) {
        VnpayCallbackOutcome outcome = paymentService.processCallback(parameters);
        return switch (outcome.getType()) {
            case PROCESSED -> response("00", "Confirm Success");
            case ALREADY_PROCESSED -> response("02", "Order already confirmed");
            case INVALID_SIGNATURE -> response("97", "Invalid signature");
            case NOT_FOUND -> response("01", "Order not found");
            case INVALID_AMOUNT -> response("04", "Invalid amount");
            case INVALID_REQUEST -> response("99", "Invalid request");
        };
    }

    @PostMapping("/{code:[A-Za-z0-9-]+}/retry")
    public Object retry(@AuthenticationPrincipal AppUserDetails user,
                        @PathVariable String code, HttpServletRequest request) {
        try {
            if (!gateway.isConfigured()) {
                throw new VnpayException("VNPay chưa được cấu hình");
            }
            VnpayPaymentRequest payment = paymentService.retry(user.getId(), code);
            RedirectView redirect = new RedirectView(gateway.createPaymentUrl(payment, request.getRemoteAddr()));
            redirect.setExposeModelAttributes(false);
            return redirect;
        } catch (VnpayException e) {
            return "redirect:/orders/" + code + "?err=payment";
        }
    }

    private static Map<String, String> response(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}
