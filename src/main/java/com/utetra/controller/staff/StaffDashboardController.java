package com.utetra.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang tam de kiem tra layout staff. Day 3 se bao ve bang quyen STAFF, Day 7 lam man hinh don hang. */
@Controller
public class StaffDashboardController {

    @GetMapping("/staff/dashboard")
    public String dashboard() {
        return "staff/dashboard";
    }
}
