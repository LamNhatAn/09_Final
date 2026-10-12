package com.utetra.controller.admin;

import com.utetra.dto.ProductForm;
import com.utetra.entity.Product;
import com.utetra.service.AdminProductService;
import com.utetra.service.EntityInUseException;
import com.utetra.service.ImageException;
import com.utetra.service.ImageStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quan ly san pham (chi ADMIN, da chan boi SecurityConfig: /admin/** hasRole ADMIN). */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService productService;

    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false, defaultValue = "") String q,
                       @RequestParam(required = false) String page,
                       Model model) {
        Long categoryId = parsePositive(category);
        Long pageNo = parsePositive(page);
        String keyword = q.trim();
        Page<Product> products = productService.search(categoryId == null ? 0L : categoryId, keyword,
                pageNo == null ? 1 : pageNo.intValue());
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.categories());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("q", keyword);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        fillFormModel(model, null);
        return "admin/products/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("productForm") ProductForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        validateExtras(form, result);
        if (!result.hasErrors()) {
            try {
                productService.create(form, form.getImage());
                redirect.addAttribute("ok", "created");
                return "redirect:/admin/products";
            } catch (ImageException e) {
                result.rejectValue("image", "image.invalid", e.getMessage());
            }
        }
        fillFormModel(model, null);
        return "admin/products/form";
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = findOr404(id);
        ProductForm form = new ProductForm();
        form.setCategoryId(product.getCategory().getId());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setBasePrice(product.getBasePrice());
        form.setAvailable(product.isAvailable());
        model.addAttribute("productForm", form);
        fillFormModel(model, product);
        return "admin/products/form";
    }

    @PostMapping("/{id:\\d+}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Product product = findOr404(id);
        validateExtras(form, result);
        if (!result.hasErrors()) {
            try {
                productService.update(product, form, form.getImage());
                redirect.addAttribute("ok", "updated");
                return "redirect:/admin/products";
            } catch (ImageException e) {
                result.rejectValue("image", "image.invalid", e.getMessage());
            }
        }
        fillFormModel(model, product);
        return "admin/products/form";
    }

    @PostMapping("/{id:\\d+}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false, defaultValue = "") String q,
                         @RequestParam(required = false) String page,
                         RedirectAttributes redirect) {
        boolean available = productService.toggleAvailable(findOr404(id));
        redirect.addAttribute("ok", available ? "shown" : "hidden");
        keepListState(redirect, category, q, page);
        return "redirect:/admin/products";
    }

    @PostMapping("/{id:\\d+}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false, defaultValue = "") String q,
                         @RequestParam(required = false) String page,
                         RedirectAttributes redirect) {
        try {
            productService.delete(findOr404(id));
            redirect.addAttribute("ok", "deleted");
        } catch (EntityInUseException | DataIntegrityViolationException e) {
            redirect.addAttribute("err", "in_use");
        }
        keepListState(redirect, category, q, page);
        return "redirect:/admin/products";
    }

    /** Anh vuot gioi han cua server (5MB): quay ve danh sach kem thong bao thay vi trang loi. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge() {
        return "redirect:/admin/products?err=too_large";
    }

    private void validateExtras(ProductForm form, BindingResult result) {
        if (form.getCategoryId() != null && !productService.categoryExists(form.getCategoryId())) {
            result.rejectValue("categoryId", "category.invalid", "Danh mục không hợp lệ");
        }
        if (form.getImage() != null && !form.getImage().isEmpty()) {
            try {
                ImageStorageService.validate(form.getImage());
            } catch (ImageException e) {
                result.rejectValue("image", "image.invalid", e.getMessage());
            }
        }
    }

    private void fillFormModel(Model model, Product product) {
        model.addAttribute("categories", productService.categories());
        model.addAttribute("product", product);
        model.addAttribute("imageConfigured", productService.imageConfigured());
    }

    private Product findOr404(Long id) {
        return productService.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    private static void keepListState(RedirectAttributes redirect, String category, String q, String page) {
        Long categoryId = parsePositive(category);
        Long pageNo = parsePositive(page);
        String keyword = q.trim();
        if (categoryId != null) {
            redirect.addAttribute("category", categoryId);
        }
        if (!keyword.isEmpty()) {
            redirect.addAttribute("q", keyword.length() > 100 ? keyword.substring(0, 100) : keyword);
        }
        if (pageNo != null && pageNo > 1) {
            redirect.addAttribute("page", pageNo);
        }
    }

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
