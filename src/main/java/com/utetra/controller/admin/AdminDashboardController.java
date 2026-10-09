package com.utetra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang tam de kiem tra layout admin. Day 3 se bao ve bang quyen ADMIN, Day 10 lam dashboard that. */
@Controller
public class AdminDashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}
