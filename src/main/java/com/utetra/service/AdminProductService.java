package com.utetra.service;

import com.utetra.dto.ProductForm;
import com.utetra.entity.Category;
import com.utetra.entity.Product;
import com.utetra.repository.CartItemRepository;
import com.utetra.repository.CategoryRepository;
import com.utetra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Quan ly san pham cho admin. Anh nam tren Cloudinary nen can giu dong bo voi CSDL:
 * - upload thanh cong nhung luu CSDL loi -> xoa anh vua upload;
 * - doi/xoa anh hoac xoa san pham -> xoa anh cu tren Cloudinary SAU KHI luu CSDL thanh cong.
 * Khong dung @Transactional o muc service vi lan goi Cloudinary khong rollback duoc.
 */
@Service
@RequiredArgsConstructor
public class AdminProductService {

    public static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final ImageStorageService imageStorage;

    public Page<Product> search(long categoryId, String keyword, int page) {
        String pattern = CatalogService.likePattern(keyword);
        Sort sort = Sort.by("id").descending();
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, sort);
        Page<Product> result = productRepository.adminSearch(categoryId, pattern, pageable);
        if (result.isEmpty() && result.getTotalPages() > 0
                && pageable.getPageNumber() >= result.getTotalPages()) {
            result = productRepository.adminSearch(categoryId, pattern,
                    PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort));
        }
        return result;
    }

    public Optional<Product> find(Long id) {
        return productRepository.findById(id);
    }

    public List<Category> categories() {
        return categoryRepository.findAll(Sort.by("id"));
    }

    public boolean categoryExists(Long id) {
        return id != null && categoryRepository.existsById(id);
    }

    public boolean imageConfigured() {
        return imageStorage.isConfigured();
    }

    public Product create(ProductForm form, MultipartFile image) {
        StoredImage uploaded = hasFile(image) ? imageStorage.upload(image) : null;
        try {
            Product product = new Product();
            apply(product, form);
            if (uploaded != null) {
                product.setImageUrl(uploaded.url());
                product.setImagePublicId(uploaded.publicId());
            }
            return productRepository.save(product);
        } catch (RuntimeException e) {
            if (uploaded != null) {
                imageStorage.deleteQuietly(uploaded.publicId());
            }
            throw e;
        }
    }

    public Product update(Product product, ProductForm form, MultipartFile image) {
        String oldPublicId = product.getImagePublicId();
        StoredImage uploaded = hasFile(image) ? imageStorage.upload(image) : null;
        try {
            apply(product, form);
            if (uploaded != null) {
                product.setImageUrl(uploaded.url());
                product.setImagePublicId(uploaded.publicId());
            } else if (form.isRemoveImage()) {
                product.setImageUrl(null);
                product.setImagePublicId(null);
            }
            Product saved = productRepository.save(product);
            if (uploaded != null || form.isRemoveImage()) {
                imageStorage.deleteQuietly(oldPublicId);
            }
            return saved;
        } catch (RuntimeException e) {
            if (uploaded != null) {
                imageStorage.deleteQuietly(uploaded.publicId());
            }
            throw e;
        }
    }

    /** Doi trang thai dang ban / ngung ban; tra ve trang thai moi. */
    public boolean toggleAvailable(Product product) {
        product.setAvailable(!product.isAvailable());
        return productRepository.save(product).isAvailable();
    }

    public void delete(Product product) {
        if (cartItemRepository.existsByProductId(product.getId())) {
            throw new EntityInUseException("Sản phẩm đang nằm trong giỏ hàng của khách");
        }
        String publicId = product.getImagePublicId();
        productRepository.delete(product);
        imageStorage.deleteQuietly(publicId);
    }

    private void apply(Product product, ProductForm form) {
        product.setCategory(categoryRepository.getReferenceById(form.getCategoryId()));
        product.setName(form.getName().trim());
        String description = form.getDescription() == null ? "" : form.getDescription().trim();
        product.setDescription(description.isEmpty() ? null : description);
        product.setBasePrice(form.getBasePrice());
        product.setAvailable(form.isAvailable());
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
