package com.utetra.service;

import com.utetra.entity.Category;
import com.utetra.entity.Product;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffProductService {

    private static final int PAGE_SIZE = 12;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<Product> list(Long categoryId, String keyword, int page) {
        long selectedCategory = categoryId == null ? 0L : categoryId;
        Sort sort = Sort.by(Sort.Order.asc("category.id"), Sort.Order.asc("name"), Sort.Order.asc("id"));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sort);
        Page<Product> result = productRepository.searchMenu(
                selectedCategory, CatalogService.likePattern(keyword), pageable);
        if (result.isEmpty() && result.getTotalPages() > 0 && pageable.getPageNumber() >= result.getTotalPages()) {
            result = productRepository.searchMenu(selectedCategory, CatalogService.likePattern(keyword),
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort));
        }
        return result;
    }

    public List<Category> categories() {
        return categoryRepository.findByActiveTrueOrderByIdAsc();
    }

    public Optional<Product> findVisible(Long id) {
        return productRepository.findPublicById(id);
    }

    @Transactional
    public boolean toggleSoldOut(Product product) {
        product.setSoldOut(!product.isSoldOut());
        return productRepository.save(product).isSoldOut();
    }
}
