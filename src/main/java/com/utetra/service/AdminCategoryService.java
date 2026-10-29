package com.utetra.service;

import com.utetra.dto.CategoryForm;
import com.utetra.entity.Category;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Quan ly danh muc cho admin. An danh muc se an luon cac san pham cua no tren trang khach. */
@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by("id"));
    }

    public Optional<Category> find(Long id) {
        return categoryRepository.findById(id);
    }

    /** categoryId -> so san pham (danh muc khong co san pham thi khong co trong map). */
    public Map<Long, Long> productCounts() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : productRepository.countByCategory()) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    /** excludeId = id dang sua (null khi them moi) de khong tu trung voi chinh no. */
    public boolean nameTaken(String name, Long excludeId) {
        String trimmed = name == null ? "" : name.trim();
        return excludeId == null
                ? categoryRepository.existsByName(trimmed)
                : categoryRepository.existsByNameAndIdNot(trimmed, excludeId);
    }

    @Transactional
    public Category create(CategoryForm form) {
        Category category = new Category();
        apply(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Category category, CategoryForm form) {
        apply(category, form);
        return categoryRepository.save(category);
    }

    /** Doi hien thi / an; tra ve trang thai moi. */
    @Transactional
    public boolean toggleActive(Category category) {
        category.setActive(!category.isActive());
        return categoryRepository.save(category).isActive();
    }

    @Transactional
    public void delete(Category category) {
        if (productRepository.existsByCategoryId(category.getId())) {
            throw new EntityInUseException("Danh mục còn sản phẩm");
        }
        categoryRepository.delete(category);
    }

    private static void apply(Category category, CategoryForm form) {
        category.setName(form.getName().trim());
        String description = form.getDescription() == null ? "" : form.getDescription().trim();
        category.setDescription(description.isEmpty() ? null : description);
        category.setActive(form.isActive());
        // Quy tắc toàn hệ thống: chỉ danh mục Trà sữa được dùng topping.
        category.setAllowToppings("Trà sữa".equalsIgnoreCase(category.getName()));
    }
}
