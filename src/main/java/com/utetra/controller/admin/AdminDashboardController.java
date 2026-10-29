package com.utetra.controller.admin;

import com.utetra.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AdminDashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.stats());
        model.addAttribute("recentOrders", dashboardService.recentOrders());
        model.addAttribute("dateTimeFormatter", DATE_TIME);
        return "admin/dashboard";
    }
}
