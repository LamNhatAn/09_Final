package com.utetra.controller.web;

import com.utetra.repository.BranchRepository;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import com.utetra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang chu tam thoi cua Day 2: chung minh JSP + SiteMesh + JPA/SQL Server hoat dong. Day 4 se lam trang chu that. */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BranchRepository branchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("branchCount", branchRepository.count());
        model.addAttribute("categoryCount", categoryRepository.count());
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("userCount", userRepository.count());
        return "home";
    }
}
