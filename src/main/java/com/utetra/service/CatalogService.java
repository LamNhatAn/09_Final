package com.utetra.service;

import com.utetra.dto.BranchInfo;
import com.utetra.dto.MenuCategoryCard;
import com.utetra.entity.Branch;
import com.utetra.entity.Category;
import com.utetra.entity.Product;
import com.utetra.entity.Size;
import com.utetra.entity.Topping;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import com.utetra.repository.SizeRepository;
import com.utetra.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/** Cac truy van doc cho phan trang khach (thuc don, chi tiet san pham, chi nhanh). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    public static final int PAGE_SIZE = 8;
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final SizeRepository sizeRepository;
    private final ToppingRepository toppingRepository;

    /** Cac kieu sap xep cho phep; gia tri la tham so `sort` tren URL (khong tin du lieu tu client). */
    public static Sort sortOf(String key) {
        return switch (key == null ? "" : key) {
            case "price_asc" -> Sort.by("basePrice").ascending().and(Sort.by("id"));
            case "price_desc" -> Sort.by("basePrice").descending().and(Sort.by("id"));
            case "name" -> Sort.by("name").ascending().and(Sort.by("id"));
            default -> Sort.by("createdAt").descending().and(Sort.by("id").descending());
        };
    }

    public static String normalizeSort(String key) {
        return switch (key == null ? "" : key) {
            case "price_asc", "price_desc", "name" -> key;
            default -> "newest";
        };
    }

    /** page tinh tu 1 (nhu URL); neu vuot qua trang cuoi thi tra ve trang cuoi. */
    public Page<Product> searchMenu(Long categoryId, String keyword, String sort, int page) {
        long cat = categoryId == null ? 0L : categoryId;
        String pattern = likePattern(keyword);
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sortOf(sort));
        Page<Product> result = productRepository.searchMenu(cat, pattern, pageable);
        if (result.isEmpty() && result.getTotalPages() > 0 && pageable.getPageNumber() >= result.getTotalPages()) {
            result = productRepository.searchMenu(cat, pattern,
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sortOf(sort)));
        }
        return result;
    }

    public List<Product> latestProducts(int limit) {
        return productRepository.findLatest(PageRequest.of(0, limit));
    }

    public Optional<Product> findPublicProduct(Long id) {
        return productRepository.findPublicById(id);
    }

    public List<Product> relatedProducts(Product product, int limit) {
        return productRepository.findRelated(product.getCategory().getId(), product.getId(), PageRequest.of(0, limit));
    }

    public List<Category> activeCategories() {
        return categoryRepository.findByActiveTrueOrderByIdAsc();
    }

    public List<MenuCategoryCard> menuCategoryCards() {
        return productRepository.findCategoryRepresentatives().stream()
                .map(product -> new MenuCategoryCard(
                        product.getCategory().getId(),
                        product.getCategory().getName(),
                        product.getImageUrl()))
                .toList();
    }

    public List<Size> sizes() {
        return sizeRepository.findAllByOrderByExtraPriceAscIdAsc();
    }

    public List<Topping> availableToppings() {
        return toppingRepository.findByAvailableTrueOrderByIdAsc();
    }

    public List<BranchInfo> activeBranches() {
        LocalTime now = LocalTime.now(ZONE);
        return branchRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(b -> new BranchInfo(b, isOpen(b, now)))
                .toList();
    }

    static boolean isOpen(Branch branch, LocalTime now) {
        LocalTime open = branch.getOpenTime();
        LocalTime close = branch.getCloseTime();
        if (open == null || close == null) {
            return true;
        }
        // Ho tro khung gio qua nua dem (vd 18:00 - 02:00)
        return open.isBefore(close)
                ? !now.isBefore(open) && now.isBefore(close)
                : !now.isBefore(open) || now.isBefore(close);
    }

    /** Escape %, _, [ va ! de nguoi dung go ky tu dac biet van tim dung nghia den. */
    static String likePattern(String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        if (k.length() > 100) {
            k = k.substring(0, 100);
        }
        String escaped = k.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");
        return "%" + escaped + "%";
    }
}
