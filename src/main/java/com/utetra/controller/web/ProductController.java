package com.utetra.controller.web;

import com.utetra.entity.Product;
import com.utetra.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final CatalogService catalogService;

    @GetMapping("/products/{id:\\d+}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = catalogService.findPublicProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
        model.addAttribute("product", product);
        model.addAttribute("sizes", catalogService.sizes());
        model.addAttribute("toppings", catalogService.availableToppings());
        model.addAttribute("related", catalogService.relatedProducts(product, 4));
        return "product-detail";
    }
}
