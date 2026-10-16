package com.utetra.service;

import com.utetra.dto.ProductForm;
import com.utetra.entity.Category;
import com.utetra.entity.Product;
import com.utetra.repository.CartItemRepository;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class AdminProductServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private CartItemRepository cartItemRepository;
    private ImageStorageService imageStorage;
    private AdminProductService service;

    private final MockMultipartFile png = new MockMultipartFile("image", "a.png", "image/png", new byte[]{1});

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        imageStorage = mock(ImageStorageService.class);
        service = new AdminProductService(productRepository, categoryRepository, cartItemRepository, imageStorage);
        when(categoryRepository.getReferenceById(1L)).thenReturn(new Category());
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static ProductForm form() {
        ProductForm f = new ProductForm();
        f.setCategoryId(1L);
        f.setName("  Tra dao  ");
        f.setDescription("   ");
        f.setBasePrice(new BigDecimal("30000"));
        return f;
    }

    @Test
    void createTrimsTextAndStoresImage() {
        when(imageStorage.upload(png)).thenReturn(new StoredImage("https://img/x.png", "utetra/products/x"));
        Product p = service.create(form(), png);
        assertEquals("Tra dao", p.getName());
        assertNull(p.getDescription());
        assertEquals("https://img/x.png", p.getImageUrl());
        assertEquals("utetra/products/x", p.getImagePublicId());
    }

    @Test
    void createDeletesUploadedImageWhenSaveFails() {
        when(imageStorage.upload(png)).thenReturn(new StoredImage("u", "pid-new"));
        when(productRepository.save(any(Product.class))).thenThrow(new IllegalStateException("db"));
        assertThrows(IllegalStateException.class, () -> service.create(form(), png));
        verify(imageStorage).deleteQuietly("pid-new");
    }

    @Test
    void createWithoutImageNeverTouchesStorage() {
        service.create(form(), null);
        verify(imageStorage, never()).upload(any());
        verify(imageStorage, never()).deleteQuietly(any());
    }

    @Test
    void updateReplacesImageThenDeletesOldOne() {
        Product existing = new Product();
        existing.setImageUrl("old-url");
        existing.setImagePublicId("pid-old");
        when(imageStorage.upload(png)).thenReturn(new StoredImage("new-url", "pid-new"));
        Product saved = service.update(existing, form(), png);
        assertEquals("pid-new", saved.getImagePublicId());
        verify(imageStorage).deleteQuietly("pid-old");
        verify(imageStorage, never()).deleteQuietly("pid-new");
    }

    @Test
    void updateKeepsOldImageWhenSaveFails() {
        Product existing = new Product();
        existing.setImagePublicId("pid-old");
        when(imageStorage.upload(png)).thenReturn(new StoredImage("new-url", "pid-new"));
        when(productRepository.save(any(Product.class))).thenThrow(new IllegalStateException("db"));
        assertThrows(IllegalStateException.class, () -> service.update(existing, form(), png));
        verify(imageStorage).deleteQuietly("pid-new");
        verify(imageStorage, never()).deleteQuietly("pid-old");
    }

    @Test
    void updateRemoveImageClearsFieldsAndDeletesOld() {
        Product existing = new Product();
        existing.setImageUrl("old-url");
        existing.setImagePublicId("pid-old");
        ProductForm f = form();
        f.setRemoveImage(true);
        Product saved = service.update(existing, f, null);
        assertNull(saved.getImageUrl());
        assertNull(saved.getImagePublicId());
        verify(imageStorage).deleteQuietly("pid-old");
    }

    @Test
    void updateWithoutImageChangeKeepsImage() {
        Product existing = new Product();
        existing.setImageUrl("old-url");
        existing.setImagePublicId("pid-old");
        Product saved = service.update(existing, form(), null);
        assertEquals("old-url", saved.getImageUrl());
        verify(imageStorage, never()).deleteQuietly(any());
    }

    @Test
    void deleteIsBlockedWhenProductIsInCart() {
        Product p = new Product();
        p.setImagePublicId("pid");
        when(cartItemRepository.existsByProductId(any())).thenReturn(true);
        assertThrows(EntityInUseException.class, () -> service.delete(p));
        verify(productRepository, never()).delete(any(Product.class));
        verify(imageStorage, never()).deleteQuietly(any());
    }

    @Test
    void deleteRemovesRowThenImage() {
        Product p = new Product();
        p.setImagePublicId("pid");
        service.delete(p);
        verify(productRepository).delete(p);
        verify(imageStorage).deleteQuietly("pid");
    }

    @Test
    void toggleFlipsAvailability() {
        Product p = new Product();
        p.setAvailable(true);
        assertEquals(false, service.toggleAvailable(p));
        assertEquals(true, service.toggleAvailable(p));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchPastLastPageFallsBackToLastPage() {
        Page<Product> pastLast = mock(Page.class);
        Page<Product> lastPage = mock(Page.class);
        when(pastLast.isEmpty()).thenReturn(true);
        when(pastLast.getTotalPages()).thenReturn(3);
        when(productRepository.adminSearch(any(Long.class), any(String.class), any(Pageable.class)))
                .thenReturn(pastLast, lastPage);

        Page<Product> result = service.search(0L, "", 999);

        assertEquals(lastPage, result);
        verify(productRepository, times(2))
                .adminSearch(any(Long.class), any(String.class), any(Pageable.class));
    }
}
