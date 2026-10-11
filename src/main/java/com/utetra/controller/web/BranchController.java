package com.utetra.controller.web;

import com.utetra.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BranchController {

    private final CatalogService catalogService;

    @GetMapping("/branches")
    public String list(Model model) {
        model.addAttribute("branches", catalogService.activeBranches());
        return "branches";
    }
}
