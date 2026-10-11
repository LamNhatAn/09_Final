package com.utetra.controller.web;

import com.utetra.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang chu: san pham moi nhat, danh muc va chi nhanh. */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CatalogService catalogService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featured", catalogService.latestProducts(8));
        model.addAttribute("categories", catalogService.activeCategories());
        model.addAttribute("branches", catalogService.activeBranches());
        return "home";
    }
}
