package com.utetra.controller.web;

import com.utetra.entity.Product;
import com.utetra.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Thuc don: loc theo danh muc, tim kiem theo ten, sap xep, phan trang (8 san pham/trang). */
@Controller
@RequiredArgsConstructor
public class MenuController {

    private final CatalogService catalogService;

    @GetMapping("/menu")
    public String menu(@RequestParam(required = false) String category,
                       @RequestParam(required = false, defaultValue = "") String q,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String page,
                       Model model) {
        Long categoryId = parsePositive(category);
        String keyword = q.trim();
        String sortKey = CatalogService.normalizeSort(sort);

        Page<Product> products = catalogService.searchMenu(categoryId, keyword, sortKey,
                parsePositive(page) == null ? 1 : parsePositive(page).intValue());

        model.addAttribute("products", products);
        model.addAttribute("categories", catalogService.activeCategories());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("q", keyword);
        model.addAttribute("sort", sortKey);
        return "menu";
    }

    /** Tham so tren URL do nguoi dung go: sai dinh dang thi coi nhu khong co (khong bao loi 400). */
    private static Long parsePositive(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long n = Long.parseLong(value.trim());
            return n > 0 && n <= Integer.MAX_VALUE ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
