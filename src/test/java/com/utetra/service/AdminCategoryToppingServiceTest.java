package com.utetra.service;

import com.utetra.dto.CategoryForm;
import com.utetra.dto.ToppingForm;
import com.utetra.entity.Category;
import com.utetra.entity.Topping;
import com.utetra.repository.CartItemRepository;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import com.utetra.repository.ToppingRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCategoryToppingServiceTest {

    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ToppingRepository toppingRepository = mock(ToppingRepository.class);
    private final CartItemRepository cartItemRepository = mock(CartItemRepository.class);
    private final AdminCategoryService categoryService = new AdminCategoryService(categoryRepository, productRepository);
    private final AdminToppingService toppingService = new AdminToppingService(toppingRepository, cartItemRepository);

    private CategoryForm categoryForm() {
        CategoryForm f = new CategoryForm();
        f.setName("  Tra moi  ");
        f.setDescription("   ");
        f.setActive(false);
        return f;
    }

    private ToppingForm toppingForm() {
        ToppingForm f = new ToppingForm();
        f.setName("  Tran chau  ");
        f.setPrice(new BigDecimal("5000"));
        return f;
    }

    @Test
    void categoryCreateTrimsAndBlankDescriptionBecomesNull() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        Category c = categoryService.create(categoryForm());
        assertEquals("Tra moi", c.getName());
        assertNull(c.getDescription());
        assertFalse(c.isActive());
        assertFalse(c.isAllowToppings());
    }

    @Test
    void onlyMilkTeaCategoryAutomaticallyAllowsToppings() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        CategoryForm form = categoryForm();
        form.setName(" Trà sữa ");

        Category category = categoryService.create(form);

        assertTrue(category.isAllowToppings());
    }

    @Test
    void categoryNameTakenUsesExcludeIdWhenEditing() {
        when(categoryRepository.existsByName("A")).thenReturn(true);
        when(categoryRepository.existsByNameAndIdNot("A", 5L)).thenReturn(false);
        assertTrue(categoryService.nameTaken(" A ", null));
        assertFalse(categoryService.nameTaken("A", 5L));
    }

    @Test
    void categoryWithProductsCannotBeDeleted() {
        Category c = new Category();
        when(productRepository.existsByCategoryId(any())).thenReturn(true);
        assertThrows(EntityInUseException.class, () -> categoryService.delete(c));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void emptyCategoryIsDeleted() {
        Category c = new Category();
        categoryService.delete(c);
        verify(categoryRepository).delete(c);
    }

    @Test
    void categoryToggleFlips() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        Category c = new Category();
        assertFalse(categoryService.toggleActive(c));
        assertFalse(c.isActive());
        assertTrue(categoryService.toggleActive(c));
    }

    @Test
    void productCountsMapsRows() {
        when(productRepository.countByCategory()).thenReturn(List.of(new Object[]{1L, 4L}, new Object[]{3, 2L}));
        Map<Long, Long> counts = categoryService.productCounts();
        assertEquals(4L, counts.get(1L));
        assertEquals(2L, counts.get(3L));
        assertNull(counts.get(2L));
    }

    @Test
    void toppingCreateTrims() {
        when(toppingRepository.save(any(Topping.class))).thenAnswer(i -> i.getArgument(0));
        Topping t = toppingService.create(toppingForm());
        assertEquals("Tran chau", t.getName());
        assertEquals(new BigDecimal("5000"), t.getPrice());
        assertTrue(t.isAvailable());
    }

    @Test
    void toppingInCartCannotBeDeleted() {
        Topping t = new Topping();
        when(cartItemRepository.existsByToppingsId(any())).thenReturn(true);
        assertThrows(EntityInUseException.class, () -> toppingService.delete(t));
        verify(toppingRepository, never()).delete(any(Topping.class));
    }

    @Test
    void unusedToppingIsDeleted() {
        Topping t = new Topping();
        toppingService.delete(t);
        verify(toppingRepository).delete(t);
    }

    @Test
    void toppingNameTakenUsesExcludeIdWhenEditing() {
        when(toppingRepository.existsByName("X")).thenReturn(true);
        when(toppingRepository.existsByNameAndIdNot("X", 2L)).thenReturn(false);
        assertTrue(toppingService.nameTaken("X", null));
        assertFalse(toppingService.nameTaken("X", 2L));
    }
}
