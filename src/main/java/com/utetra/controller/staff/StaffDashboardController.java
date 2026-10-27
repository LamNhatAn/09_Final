package com.utetra.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Chuyen URL cu cua dashboard sang man hinh xu ly don hang. */
@Controller
public class StaffDashboardController {

    @GetMapping("/staff/dashboard")
    public String dashboard() {
        return "redirect:/staff/orders";
    }
}
