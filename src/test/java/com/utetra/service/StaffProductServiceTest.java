package com.utetra.service;

import com.utetra.entity.Product;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffProductServiceTest {

    @Test
    void toggleSoldOutKeepsProductVisibleAndChangesOnlyStockStatus() {
        ProductRepository products = mock(ProductRepository.class);
        StaffProductService service = new StaffProductService(products, mock(CategoryRepository.class));
        Product product = new Product();
        product.setAvailable(true);
        product.setSoldOut(false);
        when(products.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(service.toggleSoldOut(product));
        assertTrue(product.isAvailable());
        verify(products).save(product);

        assertFalse(service.toggleSoldOut(product));
        assertTrue(product.isAvailable());
    }
}
