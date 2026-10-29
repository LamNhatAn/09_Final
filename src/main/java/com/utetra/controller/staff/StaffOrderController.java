package com.utetra.controller.staff;

import com.utetra.dto.AdminOrderDetail;
import com.utetra.entity.Order;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/staff/orders")
@RequiredArgsConstructor
public class StaffOrderController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final StaffOrderService orderService;

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserDetails staff,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String ok,
                       @RequestParam(required = false) String err,
                       Model model) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders";
        }
        String keyword = q == null ? "" : q.trim();
        model.addAttribute("orders", orderService.workQueue(staff.getBranchId(), OrderStatus.PENDING, keyword));
        model.addAttribute("q", keyword);
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        model.addAttribute("okMessage", "accepted".equals(ok)
                ? "Đã nhận đơn và chuyển sang khu vực đang pha chế." : null);
        model.addAttribute("errMessage", "cannot-accept".equals(err)
                ? "Đơn không còn ở hàng chờ hoặc chưa đủ điều kiện để nhận." : null);
        return "staff/orders/list";
    }

    @GetMapping("/processing")
    public String processing(@AuthenticationPrincipal AppUserDetails staff,
                             @RequestParam(defaultValue = "") String q,
                             @RequestParam(required = false) String ok,
                             @RequestParam(required = false) String err,
                             Model model) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders";
        }
        String keyword = q == null ? "" : q.trim();
        List<Order> preparingOrders = new ArrayList<>(
                orderService.workQueue(staff.getBranchId(), OrderStatus.CONFIRMED, keyword));
        preparingOrders.addAll(orderService.workQueue(staff.getBranchId(), OrderStatus.PREPARING, keyword));
        preparingOrders.sort(Comparator.comparing(Order::getCreatedAt).thenComparing(Order::getId));

        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("deliveringOrders",
                orderService.workQueue(staff.getBranchId(), OrderStatus.DELIVERING, keyword));
        model.addAttribute("q", keyword);
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        model.addAttribute("okMessage", "accepted".equals(ok)
                ? "Đã nhận đơn. Đơn đang chờ pha chế."
                : ("advanced".equals(ok) ? "Đơn hàng đã được chuyển sang bước tiếp theo." : null));
        model.addAttribute("errMessage", "cannot-advance".equals(err)
                ? "Không thể chuyển tiếp trạng thái của đơn này." : null);
        return "staff/orders/processing";
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

    @PostMapping("/{code:[A-Za-z0-9-]+}/accept")
    public String accept(@AuthenticationPrincipal AppUserDetails staff,
                         @PathVariable String code) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders/" + code;
        }
        try {
            orderService.acceptForPreparation(staff.getId(), staff.getBranchId(), code);
            return "redirect:/staff/orders/processing?ok=accepted";
        } catch (StaffOrderException e) {
            return "redirect:/staff/orders?err=cannot-accept";
        }
    }

    @PostMapping("/{code:[A-Za-z0-9-]+}/advance")
    public String advance(@AuthenticationPrincipal AppUserDetails staff,
                          @PathVariable String code,
                          @RequestParam(defaultValue = "detail") String returnTo) {
        if (staff.isAdmin()) {
            return "redirect:/admin/orders/" + code;
        }
        String destination = "processing".equals(returnTo)
                ? "redirect:/staff/orders/processing"
                : "redirect:/staff/orders/" + code;
        try {
            orderService.advance(staff.getId(), staff.getBranchId(), code);
            return destination + "?ok=advanced";
        } catch (StaffOrderException e) {
            return destination + "?err=cannot-advance";
        }
    }
}
