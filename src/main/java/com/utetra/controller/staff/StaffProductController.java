package com.utetra.controller.staff;

import com.utetra.entity.Product;
import com.utetra.service.StaffProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/products")
@RequiredArgsConstructor
public class StaffProductController {

    private final StaffProductService productService;

    @GetMapping
    public String list(@RequestParam(required = false) Long category,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String ok,
                       Model model) {
        model.addAttribute("products", productService.list(category, q, page));
        model.addAttribute("categories", productService.categories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("okMessage", switch (ok == null ? "" : ok) {
            case "sold-out" -> "Đã đánh dấu món tạm hết. Khách vẫn nhìn thấy món nhưng không thể đặt.";
            case "restocked" -> "Món đã có lại và khách có thể đặt bình thường.";
            default -> null;
        });
        return "staff/products/list";
    }

    @PostMapping("/{id:\\d+}/toggle-sold-out")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false) Long category,
                         @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "1") int page,
                         RedirectAttributes redirect) {
        Product product = productService.findVisible(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        boolean soldOut = productService.toggleSoldOut(product);
        redirect.addAttribute("ok", soldOut ? "sold-out" : "restocked");
        if (category != null && category > 0) redirect.addAttribute("category", category);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q.trim());
        if (page > 1) redirect.addAttribute("page", page);
        return "redirect:/staff/products";
    }
}
