package com.utetra.controller.web;

import com.utetra.dto.CustomerOrderDetail;
import com.utetra.security.AppUserDetails;
import com.utetra.service.CustomerOrderException;
import com.utetra.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final CustomerOrderService orderService;

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserDetails user,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String ok,
                       @RequestParam(required = false) String err,
                       Model model) {
        model.addAttribute("orders", orderService.list(user.getId(), page));
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        model.addAttribute("okMessage", "cancelled".equals(ok) ? "Đã hủy đơn hàng." : null);
        model.addAttribute("errMessage", errorMessage(err));
        return "orders/list";
    }

    @GetMapping("/{code:[A-Za-z0-9-]+}")
    public String detail(@AuthenticationPrincipal AppUserDetails user,
                         @PathVariable String code,
                         @RequestParam(required = false) String ok,
                         @RequestParam(required = false) String err,
                         Model model) {
        try {
            CustomerOrderDetail detail = orderService.detail(user.getId(), code);
            model.addAttribute("detail", detail);
            model.addAttribute("dateTimeFormatter", DATE_TIME);
            model.addAttribute("okMessage", "cancelled".equals(ok) ? "Đã hủy đơn hàng." : null);
            model.addAttribute("errMessage", errorMessage(err));
            return "orders/detail";
        } catch (CustomerOrderException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
    }

    @PostMapping("/{code:[A-Za-z0-9-]+}/cancel")
    public String cancel(@AuthenticationPrincipal AppUserDetails user,
                         @PathVariable String code) {
        try {
            orderService.cancel(user.getId(), code);
            return "redirect:/orders/" + code + "?ok=cancelled";
        } catch (CustomerOrderException e) {
            return "redirect:/orders/" + code + "?err=cannot-cancel";
        }
    }

    private static String errorMessage(String code) {
        if ("cannot-cancel".equals(code)) {
            return "Không thể hủy đơn này. Chỉ đơn hợp lệ đang chờ xác nhận mới được hủy.";
        }
        if ("payment".equals(code)) {
            return "Không thể tạo lại giao dịch VNPay. Hãy kiểm tra cấu hình hoặc trạng thái đơn hàng.";
        }
        return null;
    }
}
