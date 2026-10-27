package com.utetra.controller.admin;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.security.AppUserDetails;
import com.utetra.service.AdminOrderException;
import com.utetra.service.AdminOrderService;
import com.utetra.service.CatalogService;
import com.utetra.entity.enums.OrderStatus;
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
    private final CatalogService catalogService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") Long branch,
                       @RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        model.addAttribute("orders", orderService.list(branch, status, q, page));
        model.addAttribute("branches", catalogService.activeBranches());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedBranch", branch);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("q", q == null ? "" : q.trim());
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
            model.addAttribute("okMessage", "advanced".equals(ok) ? "Đã chuyển trạng thái đơn hàng." : null);
            model.addAttribute("errMessage", "cannot-advance".equals(err)
                    ? "Không thể chuyển tiếp trạng thái của đơn này." : null);
            return "admin/orders/detail";
        } catch (AdminOrderException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
    }

    @PostMapping("/{code:[A-Za-z0-9-]+}/advance")
    public String advance(@AuthenticationPrincipal AppUserDetails admin,
                          @PathVariable String code) {
        try {
            orderService.advance(admin.getId(), code);
            return "redirect:/admin/orders/" + code + "?ok=advanced";
        } catch (AdminOrderException e) {
            return "redirect:/admin/orders/" + code + "?err=cannot-advance";
        }
    }
}
