package com.utetra.controller.admin;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.security.AppUserDetails;
import com.utetra.service.AdminOrderException;
import com.utetra.service.AdminOrderService;
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
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AdminOrderService orderService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("orders", orderService.list(page));
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        return "admin/orders/list";
    }

    @GetMapping("/{code:[A-Za-z0-9-]+}")
    public String detail(@PathVariable String code,
                         @RequestParam(required = false) String ok,
                         @RequestParam(required = false) String err,
                         Model model) {
        try {
            AdminOrderDetail detail = orderService.detail(code);
            model.addAttribute("detail", detail);
            model.addAttribute("dateTimeFormatter", DATE_TIME);
            model.addAttribute("okMessage", "confirmed".equals(ok) ? "Đã xác nhận đơn hàng." : null);
            model.addAttribute("errMessage", "cannot-confirm".equals(err)
                    ? "Không thể xác nhận đơn này. Chỉ đơn đang chờ xác nhận mới được xác nhận." : null);
            return "admin/orders/detail";
        } catch (AdminOrderException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
    }

    @PostMapping("/{code:[A-Za-z0-9-]+}/confirm")
    public String confirm(@AuthenticationPrincipal AppUserDetails admin,
                          @PathVariable String code) {
        try {
            orderService.confirm(admin.getId(), code);
            return "redirect:/admin/orders/" + code + "?ok=confirmed";
        } catch (AdminOrderException e) {
            return "redirect:/admin/orders/" + code + "?err=cannot-confirm";
        }
    }
}
