package com.utetra.service;

import com.utetra.dto.AddCartItemForm;
import com.utetra.dto.CartLine;
import com.utetra.dto.CartView;
import com.utetra.entity.Cart;
import com.utetra.entity.CartItem;
import com.utetra.entity.Product;
import com.utetra.entity.Size;
import com.utetra.entity.Topping;
import com.utetra.repository.CartItemRepository;
import com.utetra.repository.CartRepository;
import com.utetra.repository.ProductRepository;
import com.utetra.repository.SizeRepository;
import com.utetra.repository.ToppingRepository;
import com.utetra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartService {

    public static final int MAX_QUANTITY = 20;
    private static final Set<Integer> ALLOWED_LEVELS = Set.of(0, 30, 50, 70, 100);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ToppingRepository toppingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartView getView(Long userId) {
        return cartRepository.findDetailedByUserId(userId).map(CartService::toView).orElseGet(CartView::empty);
    }

    @Transactional
    public void add(Long userId, AddCartItemForm form) {
        validateLevels(form.getSugarPercent(), form.getIcePercent());
        if (form.getQuantity() < 1 || form.getQuantity() > MAX_QUANTITY) {
            throw new CartException("Số lượng không hợp lệ");
        }

        Product product = productRepository.findPublicById(form.getProductId())
                .orElseThrow(() -> new CartException("Sản phẩm không còn bán"));
        Size size = sizeRepository.findById(form.getSizeId())
                .orElseThrow(() -> new CartException("Kích cỡ không hợp lệ"));
        Set<Long> toppingIds = new HashSet<>(form.getToppingIds() == null ? List.of() : form.getToppingIds());
        List<Topping> toppings = toppingIds.isEmpty()
                ? List.of() : toppingRepository.findByIdInAndAvailableTrue(toppingIds);
        if (toppings.size() != toppingIds.size()) {
            throw new CartException("Có topping không còn phục vụ");
        }

        String note = normalizeNote(form.getNote());
        Cart cart = cartRepository.findDetailedByUserId(userId).orElseGet(() -> createCart(userId));
        CartItem existing = cart.getItems().stream()
                .filter(i -> sameConfiguration(i, product.getId(), size.getId(), toppingIds,
                        form.getSugarPercent(), form.getIcePercent(), note))
                .findFirst().orElse(null);

        if (existing != null) {
            int newQuantity = existing.getQuantity() + form.getQuantity();
            if (newQuantity > MAX_QUANTITY) {
                throw new CartException("Mỗi món tối đa 20 ly");
            }
            existing.setQuantity(newQuantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setSize(size);
            item.setSugarPercent(form.getSugarPercent());
            item.setIcePercent(form.getIcePercent());
            item.setQuantity(form.getQuantity());
            item.setNote(note);
            item.setToppings(new HashSet<>(toppings));
            cart.getItems().add(item);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(Long userId, Long itemId, int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new CartException("Số lượng phải từ 1 đến 20");
        }
        CartItem item = ownedItem(userId, itemId);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void remove(Long userId, Long itemId) {
        cartItemRepository.delete(ownedItem(userId, itemId));
    }

    @Transactional
    public void clear(Long userId) {
        cartRepository.findDetailedByUserId(userId).ifPresent(cart -> cart.getItems().clear());
    }

    public static CartView toView(Cart cart) {
        List<CartLine> lines = cart.getItems().stream().map(item -> {
            BigDecimal unitPrice = unitPrice(item);
            return new CartLine(item, unitPrice, unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }).toList();
        int totalQuantity = lines.stream().mapToInt(line -> line.item().getQuantity()).sum();
        BigDecimal subtotal = lines.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(lines, totalQuantity, subtotal);
    }

    public static BigDecimal unitPrice(CartItem item) {
        BigDecimal toppingTotal = item.getToppings().stream()
                .map(Topping::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return item.getProduct().getBasePrice().add(item.getSize().getExtraPrice()).add(toppingTotal);
    }

    private Cart createCart(Long userId) {
        Cart cart = new Cart();
        cart.setUser(userRepository.getReferenceById(userId));
        return cartRepository.save(cart);
    }

    private CartItem ownedItem(Long userId, Long itemId) {
        return cartItemRepository.findOwnedDetailed(itemId, userId)
                .orElseThrow(() -> new CartException("Không tìm thấy món trong giỏ hàng"));
    }

    private static void validateLevels(Integer sugar, Integer ice) {
        if (sugar == null || ice == null || !ALLOWED_LEVELS.contains(sugar) || !ALLOWED_LEVELS.contains(ice)) {
            throw new CartException("Mức đường hoặc đá không hợp lệ");
        }
    }

    private static boolean sameConfiguration(CartItem item, Long productId, Long sizeId, Set<Long> toppingIds,
                                             int sugar, int ice, String note) {
        Set<Long> currentToppings = item.getToppings().stream().map(Topping::getId).collect(java.util.stream.Collectors.toSet());
        return Objects.equals(item.getProduct().getId(), productId)
                && Objects.equals(item.getSize().getId(), sizeId)
                && item.getSugarPercent() == sugar && item.getIcePercent() == ice
                && Objects.equals(item.getNote(), note) && currentToppings.equals(toppingIds);
    }

    private static String normalizeNote(String note) {
        String value = note == null ? "" : note.trim();
        return value.isEmpty() ? null : value;
    }
}
