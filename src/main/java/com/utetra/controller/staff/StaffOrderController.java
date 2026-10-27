package com.utetra.controller.staff;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.security.AppUserDetails;
import com.utetra.service.StaffOrderException;
import com.utetra.service.StaffOrderService;
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
@RequestMapping("/staff/orders")
@RequiredArgsConstructor
public class StaffOrderController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final StaffOrderService orderService;

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserDetails staff,
                       @RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders";
        }
        model.addAttribute("orders", orderService.list(staff.getBranchId(), status, q, page));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        return "staff/orders/list";
    }

    @GetMapping("/{code:[A-Za-z0-9-]+}")
    public String detail(@AuthenticationPrincipal AppUserDetails staff,
                         @PathVariable String code,
                         @RequestParam(required = false) String ok,
                         @RequestParam(required = false) String err,
                         Model model) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders/" + code;
        }
        try {
            AdminOrderDetail detail = orderService.detail(staff.getBranchId(), code);
            model.addAttribute("detail", detail);
            model.addAttribute("dateTimeFormatter", DATE_TIME);
            model.addAttribute("okMessage", "advanced".equals(ok) ? "Đã chuyển trạng thái đơn hàng." : null);
            model.addAttribute("errMessage", "cannot-advance".equals(err)
                    ? "Không thể chuyển tiếp trạng thái của đơn này." : null);
            return "staff/orders/detail";
        } catch (StaffOrderException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng của chi nhánh");
        }
    }

    @PostMapping("/{code:[A-Za-z0-9-]+}/advance")
    public String advance(@AuthenticationPrincipal AppUserDetails staff,
                          @PathVariable String code) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders/" + code;
        }
        try {
            orderService.advance(staff.getId(), staff.getBranchId(), code);
            return "redirect:/staff/orders/" + code + "?ok=advanced";
        } catch (StaffOrderException e) {
            return "redirect:/staff/orders/" + code + "?err=cannot-advance";
        }
    }
}
