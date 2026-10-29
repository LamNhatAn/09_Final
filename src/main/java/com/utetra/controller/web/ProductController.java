package com.utetra.controller.web;

import com.utetra.entity.Product;
import com.utetra.dto.AddCartItemForm;
import com.utetra.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final CatalogService catalogService;

    @GetMapping("/products/{id:\\d+}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String cartError,
                         Model model) {
        Product product = catalogService.findPublicProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
        model.addAttribute("product", product);
        model.addAttribute("sizes", catalogService.sizes());
        model.addAttribute("toppings", product.getCategory().isAllowToppings()
                ? catalogService.availableToppings() : java.util.List.of());
        model.addAttribute("related", catalogService.relatedProducts(product, 4));
        AddCartItemForm form = new AddCartItemForm();
        form.setProductId(product.getId());
        model.addAttribute("addCartItemForm", form);
        if ("invalid".equals(cartError)) {
            model.addAttribute("cartErrorMessage", "Không thể thêm món. Vui lòng kiểm tra lựa chọn và thử lại.");
        }
        return "product-detail";
    }
}
